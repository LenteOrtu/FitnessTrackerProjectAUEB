package com.example.ds_frontend;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;
import java.util.Locale;
import shared_classes.ActivityResults;

public class ActivityResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        ActivityResults activityResults = (ActivityResults) getIntent().getSerializableExtra("ACTIVITY_RESULTS");

        // Display total distance value.
        TextView totalDistanceValueView = findViewById(R.id.total_distance_value_textView);
        totalDistanceValueView.setText(String.format(Locale.US, "%.2f km", activityResults.getTotalDistance()));

        // Display mean speed value.
        TextView meanSpeedValueView = findViewById(R.id.mean_speed_value_textView);
        meanSpeedValueView.setText(String.format(Locale.US, "%.2f km/h", activityResults.getMeanSpeed()));

        // Display total ascent value.
        TextView totalAscentValueView = findViewById(R.id.total_ascent_value_textView);
        totalAscentValueView.setText(String.format(Locale.US, "%.2f m", activityResults.getTotalAscent()));

        // Display total activity time.
        TextView totalActivityTimeValueView = findViewById(R.id.total_activity_time_value_textView);
        totalActivityTimeValueView.setText(String.format(Locale.US, "%.2f min", activityResults.getTotalTime()));

    }
}










