package com.example.cropguard;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

public class InstructionActivity extends AppCompatActivity {

    private ImageView instructionLogoImageView;
    private Button nextButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);

        instructionLogoImageView = findViewById(R.id.instructionLogoImageView);
        nextButton = findViewById(R.id.nextButton);

        instructionLogoImageView.setImageResource(R.drawable.cameraicon); // Replace with your logo resource ID

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Go to the LoginActivity
                Intent intent = new Intent(InstructionActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }
}