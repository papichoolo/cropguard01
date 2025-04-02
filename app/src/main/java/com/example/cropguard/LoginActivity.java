package com.example.cropguard;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText;
    private Spinner stateSpinner;
    private Button loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        emailEditText = findViewById(R.id.emailEditText);
        stateSpinner = findViewById(R.id.stateSpinner);
        loginButton = findViewById(R.id.loginButton);

        // Create an ArrayList of Indian states
        ArrayList<String> indianStates = new ArrayList<>(Arrays.asList(
                "Select State", // Add "Select State" as the first item
                "Andhra Pradesh", "Arunachal Pradesh", "Assam", "Bihar", "Chhattisgarh", "Goa",
                "Gujarat", "Haryana", "Himachal Pradesh", "Jharkhand", "Karnataka", "Kerala",
                "Madhya Pradesh", "Maharashtra", "Manipur", "Meghalaya", "Mizoram", "Nagaland",
                "Odisha", "Punjab", "Rajasthan", "Sikkim", "Tamil Nadu", "Telangana", "Tripura",
                "Uttar Pradesh", "Uttarakhand", "West Bengal", "Andaman and Nicobar Islands",
                "Chandigarh", "Dadra and Nagar Haveli and Daman and Diu", "Delhi", "Jammu and Kashmir",
                "Ladakh", "Lakshadweep", "Puducherry"
        ));

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, indianStates) {
            @Override
            public boolean isEnabled(int position) {
                // Disable the first item from selection
                return position != 0;
            }

            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                // Set text color for the first item to be partially translucent
                if (position == 0) {
                    view.findViewById(android.R.id.text1).setAlpha(0.5f); // Adjust alpha as needed
                } else {
                    view.findViewById(android.R.id.text1).setAlpha(1.0f); // Normal alpha for other items
                }
                return view;
            }
        };

        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner
        stateSpinner.setAdapter(adapter);

        // Set a listener to get the selected state
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedState = (String) parent.getItemAtPosition(position);
                // You can use the selectedState here (e.g., store it in a variable)
                // Toast.makeText(LoginActivity.this, "Selected State: " + selectedState, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Another interface callback
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // For now, just proceed to the next activity without any validation
                // In a real app, you would validate the email and selected state here

                // Show a toast message (optional)
                Toast.makeText(LoginActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();

                // Start the next activity
                Intent intent = new Intent(LoginActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });
    }
}