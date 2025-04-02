package com.example.cropguard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.View;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportActivity extends AppCompatActivity {
    private TextView diseaseTextView;
    private TextView confidenceTextView;
    private LinearLayout causesLayout;
    private LinearLayout remediesLayout;
    private LinearLayout preventionLayout;
    private TextView notesTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_report);

        // Initialize views
        initViews();

        // Get JSON string from intent
        String jsonResponse = getIntent().getStringExtra("api_response");
        if (jsonResponse != null) {
            try {
                displayReport(new JSONObject(jsonResponse));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initViews() {
        diseaseTextView = findViewById(R.id.diseaseTextView);
        confidenceTextView = findViewById(R.id.confidenceTextView);
        causesLayout = findViewById(R.id.causesLayout);
        remediesLayout = findViewById(R.id.remediesLayout);
        preventionLayout = findViewById(R.id.preventionLayout);
        notesTextView = findViewById(R.id.notesTextView);
    }

    private void displayReport(JSONObject report) throws JSONException {
        // Display disease and confidence
        diseaseTextView.setText("Disease: " + report.getString("disease"));
        confidenceTextView.setText("Confidence: " + report.getString("confidence") + "%");

        // Display causes
        JSONArray causes = report.getJSONArray("causes");
        for (int i = 0; i < causes.length(); i++) {
            JSONObject cause = causes.getJSONObject(i);
            addCauseView(cause);
        }

        // Display remedies
        JSONObject remedies = report.getJSONObject("remedies");
        displayRemedies(remedies);

        // Display prevention steps
        JSONArray prevention = report.getJSONArray("prevention");
        for (int i = 0; i < prevention.length(); i++) {
            addPreventionView(prevention.getString(i));
        }

        // Display notes
        notesTextView.setText(report.getString("notes"));
    }

    private void addCauseView(JSONObject cause) throws JSONException {
        View causeView = LayoutInflater.from(this).inflate(R.layout.item_cause, null);
        TextView causeTitle = causeView.findViewById(R.id.causeTitleTextView);
        TextView causeDescription = causeView.findViewById(R.id.causeDescriptionTextView);

        causeTitle.setText(cause.getString("cause"));
        causeDescription.setText(cause.getString("description"));
        causesLayout.addView(causeView);
    }

    private void displayRemedies(JSONObject remedies) throws JSONException {
        // Display chemical remedies
        JSONArray chemicalRemedies = remedies.getJSONArray("chemical");
        for (int i = 0; i < chemicalRemedies.length(); i++) {
            JSONObject remedy = chemicalRemedies.getJSONObject(i);
            addRemedyView("Chemical: " + remedy.getString("product"),
                    "Dosage: " + remedy.getString("dosage"));
        }

        // Display organic remedies
        JSONArray organicRemedies = remedies.getJSONArray("organic");
        for (int i = 0; i < organicRemedies.length(); i++) {
            JSONObject remedy = organicRemedies.getJSONObject(i);
            addRemedyView("Organic: " + remedy.getString("method"),
                    remedy.getString("description"));
        }
    }

    private void addRemedyView(String title, String description) {
        View remedyView = LayoutInflater.from(this).inflate(R.layout.item_remedy, null);
        TextView remedyTitle = remedyView.findViewById(R.id.remedyTitleTextView);
        TextView remedyDescription = remedyView.findViewById(R.id.remedyDescriptionTextView);

        remedyTitle.setText(title);
        remedyDescription.setText(description);
        remediesLayout.addView(remedyView);
    }

    private void addPreventionView(String prevention) {
        TextView preventionText = new TextView(this);
        preventionText.setText("• " + prevention);
        preventionText.setTextSize(16);
        preventionText.setPadding(0, 8, 0, 8);
        preventionLayout.addView(preventionText);
    }
}