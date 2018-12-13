package com.example.roomi.roomi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;


public class Home extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private TextView welcomeString, fullNameMenu, emailMenu;
    private NavigationView navigationView;
    private View headerView;
    private FirebaseUser fbUser;

    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private User user;

    private String languageCode;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        fbUser = mAuth.getCurrentUser();

        if (fbUser == null) {
            startActivity(new Intent(Home.this, MainActivity.class));
            finish();
        } else {
            changeAppLanguage();

            setContentView(R.layout.activity_home);
            getWindow().setBackgroundDrawableResource(R.drawable.gradient);

            mDrawerLayout = findViewById(R.id.drawer_layout);

            ActionBar actionbar = getSupportActionBar();
            actionbar.setDisplayHomeAsUpEnabled(true);
            actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_hamburger);

            findViews();
            getDatabase();
            logoutListener();
            retrieveData();
        }

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        int id = menuItem.getItemId();

                        if (id == R.id.nav_home) {
                            Intent myIntent = new Intent(Home.this, RoomSelector.class);
                            Home.this.startActivity(myIntent);
                        } else if (id == R.id.nav_security) {
                            // Goes to Security Activity
                            Intent security = new Intent(getApplicationContext(), SecuritySelector.class);
                            startActivity(security);
                        } else if (id == R.id.nav_settings) {
                            // Goes to Settings Page
                            Intent settings = new Intent(getApplicationContext(), Settings.class);
                            startActivity(settings);
                        } else if (id == R.id.nav_aboutus) {
                            Intent mAboutUs = new Intent(Home.this, AboutUs.class);
                            startActivity(mAboutUs);
                        } else if (id == R.id.nav_logout) {
                            // Logs out and displays the Log In Screen

                            mAuth.signOut();
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else if (id == R.id.nav_exit) {
                            finishAffinity();
                        }

                        mDrawerLayout.closeDrawers();
                        return true;
                    }
                }
        );

        View homeButton = findViewById(R.id.home_button);

        homeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(Home.this, RoomSelector.class);
                Home.this.startActivity(myIntent);
            }
        });

        View securityButton = findViewById(R.id.security_button);

        securityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent security = new Intent(getApplicationContext(), SecuritySelector.class);
                startActivity(security);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if(mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void findViews() {
        welcomeString = findViewById(R.id.welcomeString);
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        fullNameMenu = headerView.findViewById(R.id.fullNameUser);
        emailMenu = headerView.findViewById(R.id.emailUser);
    }

    public void getFromSharedPreference() {
        SharedPreferences prefs = getSharedPreferences("language", MODE_PRIVATE);
        languageCode = prefs.getString(fbUser.getEmail(), "en");
    }

    private void getDatabase() {
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/" + fbUser.getUid());
    }

    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user != null) {
                    welcomeString.setText(getString(R.string.welcome_home) + " " + user.getFirstName());
                    fullNameMenu.setText(user.getFirstName() + " " + user.getLastName());
                    emailMenu.setText(user.getEmail());
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getApplicationContext(), "Database access error!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void changeAppLanguage() {
        getFromSharedPreference();
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private void logoutListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(Home.this, MainActivity.class));
                    finish();
                }
            }
        };
    }



}
