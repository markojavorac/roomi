package com.example.roomi.roomi;

import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class SecurityRoomSelector extends AppCompatActivity {

    private DrawerLayout mDrawerLayout;
    private FirebaseDatabase database;
    private DatabaseReference dbRef;
    private int i;
    private String[] keyList;

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener authListener;
    private TextView fullNameMenu, emailMenu;
    private NavigationView navigationView;
    private View headerView;

    private DatabaseReference dbUserRef;
    private User user;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_security_room_selector);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mAuth = FirebaseAuth.getInstance();

        setTitle(R.string.rooms);

        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(getApplicationContext(), MainActivity.class));
            finish();
        } else {
            getDatabase();
            retrieveData();
            logoutListener();
            findViews();
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void getDatabase() {
        database = FirebaseDatabase.getInstance();
        dbUserRef = database.getReference("users/" + mAuth.getUid());
        dbRef = database.getReference("users/" + mAuth.getCurrentUser().getUid() + "/rooms");
    }

    private void findViews() {
        navigationView = findViewById(R.id.nav_view);
        headerView = navigationView.getHeaderView(0);
        fullNameMenu = headerView.findViewById(R.id.fullNameUser);
        emailMenu = headerView.findViewById(R.id.emailUser);
    }

    private void retrieveData() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                fetchRooms(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.d("roomi", "Data retrieval error...", databaseError.toException());
            }
        });

        dbUserRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
                if (user != null) {
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

    private void fetchRooms(DataSnapshot dataSnapshot) {
        List<RoomDatastructure> roomList = new ArrayList<>();
        keyList = new String[(int) dataSnapshot.getChildrenCount()];
        i = 0;
        roomList.clear();
        for (DataSnapshot roomSnapShot: dataSnapshot.getChildren()) {
            RoomDatastructure room = roomSnapShot.getValue(RoomDatastructure.class);
            keyList[i] = roomSnapShot.getKey();
            roomList.add(room);
            i++;
        }
        generateRoomButtons(roomList, keyList);
    }

    private void generateRoomButtons(List<RoomDatastructure> roomList, final String[] keyList) {
        i = 0;
        LinearLayout buttonContainer = findViewById(R.id.button_container);
        buttonContainer.removeAllViews();
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(900, 200);
        params.setMargins(0, 0, 0, 65);

        for (RoomDatastructure room: roomList) {
            final String key = keyList[i];
            final String name = room.getName();
            final int accessLevel = room.getAccessLevel();
            Button button = new Button(this);
            button.setTag(room.getName());
            button.setBackgroundResource(R.drawable.element_background_dark);
            button.setLayoutParams(params);
            button.setText(room.getName());
            button.setTextSize(20);
            button.setTextColor(Color.WHITE);
            button.setTransformationMethod(null);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(SecurityRoomSelector.this, SecurityRoomSettings.class);
                    intent.putExtra("key", key);
                    intent.putExtra("name", name);
                    intent.putExtra("accessLevel", accessLevel);
                    startActivity(intent);
                }
            });
            i++;
            buttonContainer.addView(button);
        }

        Button addSecurityRoomButton = new Button(this);
        addSecurityRoomButton.setTag("add_room");
        addSecurityRoomButton.setBackgroundResource(R.drawable.element_background_dark);
        addSecurityRoomButton.setLayoutParams(params);
        addSecurityRoomButton.setText("+");
        addSecurityRoomButton.setTextSize(20);
        addSecurityRoomButton.setTextColor(Color.WHITE);
        addSecurityRoomButton.setTransformationMethod(null);

        addSecurityRoomButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SecurityRoomSelector.this, SecurityAddRoom.class);
                startActivity(intent);
            }
        });

        buttonContainer.addView(addSecurityRoomButton);
    }

    private void logoutListener() {
        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                    finish();
                }
            }
        };
    }
}
