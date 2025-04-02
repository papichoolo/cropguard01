package com.example.cropguard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.support.common.ops.NormalizeOp;
import org.tensorflow.lite.support.image.ImageProcessor;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.image.ops.ResizeOp;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class TFLiteClassifier extends AppCompatActivity {

    private Interpreter tflite;
    private TextView diseaseTextView, confidenceTextView, notesTextView;
    private LinearLayout causesLayout, remediesLayout, preventionLayout;
    private Button nextButton;
    private static final String API_URL = "https://cropguard-1wyl.onrender.com/analyze/";

    private static final String[] CLASS_LABELS = {
            "Apple___Apple_scab", "Apple___Black_rot", "Apple___Cedar_apple_rust", "Apple___healthy",
            "Blueberry___healthy", "Cherry_(including_sour)___Powdery_mildew", "Cherry_(including_sour)___healthy",
            "Corn_(maize)___Cercospora_leaf_spot Gray_leaf_spot", "Corn_(maize)___Common_rust_", "Corn_(maize)___Northern_Leaf_Blight",
            "Corn_(maize)___healthy", "Grape___Black_rot", "Grape___Esca_(Black_Measles)", "Grape___Leaf_blight_(Isariopsis_Leaf_Spot)",
            "Grape___healthy", "Orange___Haunglongbing_(Citrus_greening)", "Peach___Bacterial_spot", "Peach___healthy",
            "Pepper,_bell___Bacterial_spot", "Pepper,_bell___healthy", "Potato___Early_blight", "Potato___Late_blight",
            "Potato___healthy", "Raspberry___healthy", "Soybean___healthy", "Squash___Powdery_mildew",
            "Strawberry___Leaf_scorch", "Strawberry___healthy", "Tomato___Bacterial_spot", "Tomato___Early_blight",
            "Tomato___Late_blight", "Tomato___Leaf_Mold", "Tomato___Septoria_leaf_spot", "Tomato___Spider_mites Two-spotted_spider_mite",
            "Tomato___Target_Spot", "Tomato___Tomato_Yellow_Leaf_Curl_Virus", "Tomato___Tomato_mosaic_virus", "Tomato___healthy"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tflite_classifier);

        initViews();
        loadModel();
        processIntent();
        setupNextButton();
    }

    private void initViews() {
        diseaseTextView = findViewById(R.id.diseaseTextView);
        confidenceTextView = findViewById(R.id.confidenceTextView);
        causesLayout = findViewById(R.id.causesLayout);
        remediesLayout = findViewById(R.id.remediesLayout);
        preventionLayout = findViewById(R.id.preventionLayout);
        notesTextView = findViewById(R.id.notesTextView);
        nextButton = findViewById(R.id.nextButton);
    }

    private void loadModel() {
        try {
            TFLiteHelper tfliteHelper = new TFLiteHelper(this);
            tfliteHelper.loadModel("inceptionV3.tflite");
            tflite = tfliteHelper.getInterpreter();
        } catch (IOException e) {
            Log.e("TFLiteClassifier", "Error loading model", e);
            diseaseTextView.setText(R.string.error_loading_model);
        }
    }

    private void processIntent() {
        Intent intent = getIntent();
        Bitmap bitmap = intent.getParcelableExtra("imageBitmap");
        if (bitmap != null) {
            runInference(bitmap);
        } else {
            diseaseTextView.setText(R.string.error_bitmap_null);
        }
    }

    private void setupNextButton() {
        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                startActivity(new Intent(TFLiteClassifier.this, GovernmentSchemesActivity.class));
            });
        } else {
            Log.e("TFLiteClassifier", "Next button is null");
        }
    }

    private void runInference(Bitmap bitmap) {
        TensorImage inputImage = TensorImage.fromBitmap(bitmap);
        ImageProcessor imageProcessor = new ImageProcessor.Builder()
                .add(new ResizeOp(299, 299, ResizeOp.ResizeMethod.BILINEAR))
                .add(new NormalizeOp(0, 255))
                .build();
        TensorImage processedImage = imageProcessor.process(inputImage);
        TensorBuffer outputBuffer = TensorBuffer.createFixedSize(new int[]{1, 1024}, DataType.UINT8);

        tflite.run(processedImage.getBuffer(), outputBuffer.getBuffer().rewind());

        // Get the prediction results
        float[] confidences = outputBuffer.getFloatArray();
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidences.length; i++) {
            if (confidences[i] > maxConfidence) {
                maxConfidence = confidences[i];
                maxPos = i;
            }
        }

        String predictedClass = CLASS_LABELS[maxPos];
        float confidence = maxConfidence;
        String imageSize = "299x299";

        // Create JSON object with prediction data
        try {
            JSONObject predictionData = new JSONObject();
            predictionData.put("class_name", predictedClass);
            predictionData.put("confidence", confidence);
            predictionData.put("image_size", imageSize);

            // Send data to API
            new SendPredictionDataTask().execute(predictionData);

            // Continue with existing display logic
            String jsonOutput = getJsonOutput(outputBuffer);
            if (jsonOutput == null) return;
            parseAndDisplayJson(jsonOutput);

        } catch (JSONException e) {
            Log.e("TFLiteClassifier", "Error creating prediction JSON", e);
            Toast.makeText(this, "Error processing prediction data", Toast.LENGTH_SHORT).show();
        }
    }

    private String getJsonOutput(TensorBuffer outputBuffer) {
        try {
            String jsonOutput = new String(outputBuffer.getBuffer().array(), StandardCharsets.UTF_8);
            Log.d("JSON_OUTPUT", jsonOutput);
            return jsonOutput;
        } catch (Exception e) {
            Log.e("TFLiteClassifier", "Error reading model output", e);
            diseaseTextView.setText(R.string.error_reading_model_output);
            return null;
        }
    }

    private void parseAndDisplayJson(String jsonOutput) {
        try {
            JSONObject jsonObject = new JSONObject(jsonOutput);
            displayDiseaseAndConfidence(jsonObject);
            notesTextView.setText(jsonObject.optString("notes", ""));
            parseAndDisplayCauses(jsonObject);
            parseAndDisplayRemedies(jsonObject);
            parseAndDisplayPrevention(jsonObject);
        } catch (JSONException e) {
            Log.e("TFLiteClassifier", "Error parsing JSON", e);
            diseaseTextView.setText(getString(R.string.error_parsing_json, e.getMessage()));
        }
    }

    private void displayDiseaseAndConfidence(JSONObject jsonObject) {
        diseaseTextView.setText(getString(R.string.disease_text, jsonObject.optString("disease", getString(R.string.unknown_disease))));
        confidenceTextView.setText(getString(R.string.confidence_text, jsonObject.optString("confidence", getString(R.string.na))));
        Log.d("DISEASE", String.format("Disease: %s", jsonObject.optString("disease", "")));
        Log.d("CONFIDENCE", String.format("Confidence: %s", jsonObject.optString("confidence", "")));
    }

    private void parseAndDisplayCauses(JSONObject jsonObject) {
        JSONArray causesArray = jsonObject.optJSONArray("causes");
        causesLayout.removeAllViews();
        if (causesArray != null) {
            for (int i = 0; i < causesArray.length(); i++) {
                JSONObject causeObject = causesArray.optJSONObject(i);
                if (causeObject != null) {
                    TextView causeTextView = new TextView(this);
                    causeTextView.setText(getString(R.string.cause_text, causeObject.optString("cause", "")));
                    causesLayout.addView(causeTextView);
                    TextView descriptionTextView = new TextView(this);
                    descriptionTextView.setText(causeObject.optString("description", ""));
                    causesLayout.addView(descriptionTextView);
                }
            }
        }
        Log.d("CAUSES_COUNT", String.format("Causes count: %d", causesLayout.getChildCount()));
    }

    private void parseAndDisplayRemedies(JSONObject jsonObject) {
        JSONObject remediesObject = jsonObject.optJSONObject("remedies");
        remediesLayout.removeAllViews();
        if (remediesObject != null) {
            // ... (rest of the remedies parsing code) ...
        }
        Log.d("REMEDIES_COUNT", String.format("Remedies count: %d", remediesLayout.getChildCount()));
    }

    private void parseAndDisplayPrevention(JSONObject jsonObject) {
        JSONArray preventionArray = jsonObject.optJSONArray("prevention");
        preventionLayout.removeAllViews();
        if (preventionArray != null) {
            for (int i = 0; i < preventionArray.length(); i++) {
                TextView preventionTextView = new TextView(this);
                preventionTextView.setText(preventionArray.optString(i, ""));
                preventionLayout.addView(preventionTextView);
            }
        }
        Log.d("PREVENTION_COUNT", String.format("Prevention count: %d", preventionLayout.getChildCount()));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (tflite != null) {
            tflite.close();
        }
    }

    private class SendPredictionDataTask extends AsyncTask<JSONObject, Void, String> {
        @Override
        protected String doInBackground(JSONObject... params) {
            try {
                URL url = new URL(API_URL);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = params[0].toString().getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    // Read response if needed
                    // For now, we'll just return success
                    return "Success";
                } else {
                    return "Error: " + responseCode;
                }

            } catch (Exception e) {
                Log.e("TFLiteClassifier", "Error sending prediction data", e);
                return "Error: " + e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (!result.startsWith("Success")) {
                Toast.makeText(TFLiteClassifier.this,
                        "Error sending prediction data: " + result,
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}