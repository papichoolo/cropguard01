package com.example.cropguard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class ImagePreviewActivity extends AppCompatActivity {

    private ImageView previewImageView;
    private Button uploadButton;
    private Button clickAgainButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_preview);

        previewImageView = findViewById(R.id.previewImageView);
        uploadButton = findViewById(R.id.uploadButton);
        clickAgainButton = findViewById(R.id.clickAgainButton);

        // Retrieve the bitmap from the intent
        Bitmap imageBitmap = getIntent().getParcelableExtra("imageBitmap");
        if (imageBitmap != null) {
            previewImageView.setImageBitmap(imageBitmap);
        }

        uploadButton.setOnClickListener(v -> {
            // Pass the bitmap to TFLiteClassifier
            Intent intent = new Intent(ImagePreviewActivity.this, TFLiteClassifier.class);
            intent.putExtra("imageBitmap", imageBitmap);
            startActivity(intent);
        });

        clickAgainButton.setOnClickListener(v -> {
            // Go back to CameraActivity to capture a new photo
            finish(); // This will go back to the previous activity (CameraActivity)
        });
    }
}