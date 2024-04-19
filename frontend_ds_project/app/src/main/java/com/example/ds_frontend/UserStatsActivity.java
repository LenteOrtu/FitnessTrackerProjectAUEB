package com.example.ds_frontend;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Locale;
import shared_classes.ActivitiesStats;

public class UserStatsActivity extends AppCompatActivity {

    static int max_bar_width_dp = 350;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stats);

        Bundle bundle = getIntent().getBundleExtra("MEAN_STATS");
        ActivitiesStats userStats = (ActivitiesStats) bundle.getSerializable("USER_STATS");
        ActivitiesStats globalStats = (ActivitiesStats) bundle.getSerializable("GLOBAL_STATS");

        /* TOTAL DISTANCE BAR GRAPH */

        double maxDistance = Math.max(userStats.getTotalDistance(), globalStats.getTotalDistance());
        double distance_user_bar_width = (userStats.getTotalDistance()/maxDistance) * max_bar_width_dp;
        double distance_global_bar_width = (globalStats.getTotalDistance()/maxDistance) * max_bar_width_dp;

        View distanceUserBarView = findViewById(R.id.distance_user_barView);
        adjustBarView(distanceUserBarView, distance_user_bar_width);

        View distanceGlobalBarView = findViewById(R.id.distance_global_barView);
        adjustBarView(distanceGlobalBarView, distance_global_bar_width);

        TextView userTotalDistanceTextView = findViewById(R.id.user_total_distance_textView);
        userTotalDistanceTextView.setText(String.format(Locale.US, "%.2fkm (YOU)", userStats.getTotalDistance()));

        TextView globalTotalDistanceTextView = findViewById(R.id.global_total_distance_textView);
        globalTotalDistanceTextView.setText(String.format(Locale.US, "%.2fkm (USER AVG)", globalStats.getTotalDistance()));

        /* TOTAL ACTIVITY TIME BAR GRAPH */

        double maxTime = Math.max(userStats.getTotalActivityTime(), globalStats.getTotalActivityTime());
        double time_user_bar_width = (userStats.getTotalActivityTime()/maxTime) * max_bar_width_dp;
        double time_global_bar_width = (globalStats.getTotalActivityTime()/maxTime) * max_bar_width_dp;

        // In case total activity time is 60 minutes or more, convert to hr(s) and change measurement unit displayed.

        String user_time_measurement_unit = "min";
        double user_total_activity_time = userStats.getTotalActivityTime();
        if (user_total_activity_time >= 60){
            user_time_measurement_unit = "hr(s)";
            user_total_activity_time /= 60; // Convert to hrs.
        }

        String global_time_measurement_unit = "min";
        double global_total_activity_time = globalStats.getTotalActivityTime();
        if (global_total_activity_time >= 60){
            global_time_measurement_unit = "hr(s)";
            global_total_activity_time /= 60; // Convert to hrs.
        }

        View timeUserBarView = findViewById(R.id.activity_time_user_barView);
        adjustBarView(timeUserBarView, time_user_bar_width);

        View timeGlobalBarView = findViewById(R.id.activity_time_global_barView);
        adjustBarView(timeGlobalBarView, time_global_bar_width);

        TextView userTotalTimeTextView = findViewById(R.id.user_total_activity_time_textView);
        userTotalTimeTextView.setText(String.format(Locale.US, "%.2f%s (YOU)", user_total_activity_time, user_time_measurement_unit));

        TextView globalTotalTimeTextView = findViewById(R.id.global_total_activity_time_textView);
        globalTotalTimeTextView.setText(String.format(Locale.US, "%.2f%s (USER AVG)", global_total_activity_time, global_time_measurement_unit));

        /* TOTAL ASCENT BAR GRAPH */

        double maxAscent = Math.max(userStats.getTotalAscent(), globalStats.getTotalAscent());
        double ascent_user_bar_width = (userStats.getTotalAscent()/maxAscent) * max_bar_width_dp;
        double ascent_global_bar_width = (globalStats.getTotalAscent()/maxAscent) * max_bar_width_dp;

        // In case total ascent is 1000 meters or more, convert to km and change measurement unit displayed.

        String user_ascent_measurement_unit = "m";
        double user_total_ascent = userStats.getTotalAscent();
        if (user_total_ascent >= 1000){
            user_ascent_measurement_unit = "km";
            user_total_ascent /= 1000; // Convert to km.
        }

        String global_ascent_measurement_unit = "m";
        double global_total_ascent = globalStats.getTotalAscent();
        if (global_total_ascent >= 1000){
            global_ascent_measurement_unit = "km";
            global_total_ascent /= 1000; // Convert to km.
        }

        View ascentUserBarView = findViewById(R.id.ascent_user_barView);
        adjustBarView(ascentUserBarView, ascent_user_bar_width);

        View ascentGlobalBarView = findViewById(R.id.ascent_global_barView);
        adjustBarView(ascentGlobalBarView, ascent_global_bar_width);

        TextView userTotalAscentTextView = findViewById(R.id.user_total_ascent_textView);
        userTotalAscentTextView.setText(String.format(Locale.US, "%.2f%s (YOU)", user_total_ascent, user_ascent_measurement_unit));

        TextView globalTotalAscentTextView = findViewById(R.id.global_total_ascent_textView);
        globalTotalAscentTextView.setText(String.format(Locale.US, "%.2f%s(USER AVG)", global_total_ascent, global_ascent_measurement_unit));

    }

    private void adjustBarView(View barView, double widthDp){
        // Convert width from dp to pixels.
        double widthPixels = widthDp * barView.getContext().getResources().getDisplayMetrics().density;

        // Set width.
        ViewGroup.LayoutParams layoutParams = barView.getLayoutParams();
        layoutParams.width = (int) widthPixels;
        barView.setLayoutParams(layoutParams);
    }
}









