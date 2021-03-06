package com.example.roomi.roomi;

import android.content.Intent;
import android.net.wifi.hotspot2.pps.HomeSp;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class AddRoom extends AppCompatActivity {

    private FirebaseDatabase database;
    private DatabaseReference dbRef;

    private EditText nameInput;
    private EditText temperatureInput;
    private EditText brightnessInput;
    private Button submitButton;
    private Button cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_room);
        getWindow().setBackgroundDrawableResource(R.drawable.gradient);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setTitle(R.string.add_a_room);
        getDatabase();
        getElements();

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateData()) {
                    String name = nameInput.getText().toString();
                    int temperature = Integer.parseInt(temperatureInput.getText().toString());
                    int brightness = Integer.parseInt(brightnessInput.getText().toString());

                    DatabaseReference newRoom = dbRef.push();
                    newRoom.setValue(new HomeRoomDataStructure(name, temperature, brightness));
                    Toast toast = Toast.makeText(getApplicationContext(), name + " created!", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
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

    private void getElements() {
        nameInput = findViewById(R.id.add_room_name_input);
        temperatureInput = findViewById(R.id.add_room_temperature_input);
        brightnessInput = findViewById(R.id.add_room_brightness_input);
        submitButton = findViewById(R.id.add_room_button);
        cancelButton = findViewById(R.id.cancel_add_room_button);
    }

    private void getDatabase() {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser fbUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();
        dbRef = database.getReference("users/" + fbUser.getUid() + "/rooms/home");
    }

    private boolean validateData() {
        int temperature = 0;
        int brightness = -1;
        int nameLen = nameInput.getText().toString().length();

        if (!temperatureInput.getText().toString().equals("")) {
            try {
                temperature = Integer.parseInt(temperatureInput.getText().toString());
            } catch (Exception e) {
                Log.d("IntParse1", e.toString());
            }
        }

        if (!brightnessInput.getText().toString().equals("")) {
            try {
                brightness = Integer.parseInt(brightnessInput.getText().toString());
            } catch (Exception e) {
                Log.d("IntParse2", e.toString());
            }
        }

        if (nameLen <= 0 || nameLen > 25) {
            nameInput.setError("Please enter a name between 1 and 25 characters");
            return false;
        }
        if (temperature < 15 || temperature > 25) {
            temperatureInput.setError("Please enter a valid temperature");
            return false;
        }
        if (brightness < 0 || brightness > 100) {
            brightnessInput.setError("Please enter a valid brightness");
            return false;
        }
        return true;
    }
}
