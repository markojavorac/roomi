package com.example.roomi.roomi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private ProgressBar progressBar;
    private EditText inputEmail, inputPassword;
    private Button loginButton;
    private TextView createAccount;
    private String languageCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ActionBar actionBar = getSupportActionBar();
        actionBar.hide();

        // Getting Auth Instance
        mAuth = FirebaseAuth.getInstance();
        getFromSharedPreference();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, Home.class));
            finish();
        }

        // Setting the view
        setTheme(R.style.AppTheme);
        setContentView(R.layout.activity_login);

        getViews();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utilities.hideKeyboard(MainActivity.this);
                login();
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, Registration.class));
            }
        });

    }

    public void saveToSharedPreference() {
        SharedPreferences.Editor editor = getSharedPreferences("language", MODE_PRIVATE).edit();
        editor.putString("lang", languageCode);
        editor.apply();
    }

    public void getFromSharedPreference() {
        SharedPreferences prefs = getSharedPreferences("language", MODE_PRIVATE);
        languageCode = prefs.getString("lang", "en");
    }

    // Get views on the activity
    private void getViews() {
        // Obtaining the login button
        loginButton = findViewById(R.id.login_button);

        //Obtaining view objects
        inputEmail = findViewById(R.id.email);
        inputPassword = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);
        createAccount = findViewById(R.id.createAccount);
    }

    // Login Method
    private void login() {
        String email = inputEmail.getText().toString();
        final String password = inputPassword.getText().toString();

        if (email.isEmpty()) {
            inputEmail.setError("Please enter an email address");
//            Toast.makeText(getApplicationContext(), "Please enter an email address!", Toast.LENGTH_LONG).show();
            return;
        }

        if (password.isEmpty()) {
            inputPassword.setError("Please enter a password");
            Toast.makeText(getApplicationContext(), "Please enter password!", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);
                        if (!task.isSuccessful()) {
                            if (password.length() < 6) {
                                inputPassword.setError("Password too short! Enter at least 6 characters!");
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to authenticate. Check your email and password or sign up!", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Intent home = new Intent(MainActivity.this, Home.class);
                            startActivity(home);
                            finish();
                        }
                    }
                });
    }


}