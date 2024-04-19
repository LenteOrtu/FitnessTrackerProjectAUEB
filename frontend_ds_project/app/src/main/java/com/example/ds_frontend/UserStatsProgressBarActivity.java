package com.example.ds_frontend;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class UserStatsProgressBarActivity extends AppCompatActivity {

    private Handler mainThreadHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_stats_progress_bar);

        mainThreadHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                // Receive MeanStats objects.
                Bundle bundle = msg.getData();

                // Navigate to next activity to display user stats and global stats, also forward the MeanStats objects.
                Intent viewUserStatsIntent = new Intent(UserStatsProgressBarActivity.this, UserStatsActivity.class);
                viewUserStatsIntent.putExtra("MEAN_STATS", bundle);
                startActivity(viewUserStatsIntent);

                return true;
            }
        });

        String username = getIntent().getExtras().getString("USERNAME");
        UserStatsRequestThread userStatsRequestThread = new UserStatsRequestThread(mainThreadHandler, username);
        userStatsRequestThread.start();
    }
}