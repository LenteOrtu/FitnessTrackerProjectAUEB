package com.example.ds_frontend;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import shared_classes.ActivityResults;


public class MainActivity extends AppCompatActivity {

    Button viewStatsButton;
    Button uploadButton;
    boolean usernameDefined = false;
    String username = "";
    Handler mainThreadHandler;
    static final int REQUEST_GPX_GET = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewStatsButton = (Button) findViewById(R.id.view_stats_button);
        uploadButton = (Button) findViewById(R.id.upload_button);

        // Check if file containing username already exists and get username if it does.
        File path = getApplicationContext().getFilesDir();
        if ((new File(path, "username.txt")).exists()){
            username = readFromFile("username.txt");
            usernameDefined = true;
        }

        mainThreadHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {

                ActivityResults activityResults = (ActivityResults) msg.obj;
                if (!usernameDefined) {
                    username = activityResults.getUsername();
                    writeToFile("username.txt", username); // Write username to text file so username persists between sessions.
                    usernameDefined = true;
                }
                Intent viewActivityResultsIntent = new Intent(uploadButton.getContext(), ActivityResultsActivity.class);
                viewActivityResultsIntent.putExtra("ACTIVITY_RESULTS", activityResults);
                startActivity(viewActivityResultsIntent);

                return true;
            }
        });
    }

    @Override
    protected void onResume(){
        super.onResume();

        viewStatsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (usernameDefined){
                    Intent userStatsProgressBarIntent = new Intent(viewStatsButton.getContext(), UserStatsProgressBarActivity.class);
                    userStatsProgressBarIntent.putExtra("USERNAME", username);
                    startActivity(userStatsProgressBarIntent);
                }
            }
        });

        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectGPX();
            }
        });
    }


    @SuppressWarnings("deprecation")
    private void selectGPX(){
        Intent selectGPXFileIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        selectGPXFileIntent.setType("application/octet-stream"); // application/gpx+xml did not work.
        selectGPXFileIntent.addCategory(Intent.CATEGORY_OPENABLE);

        // When asynch i will remove the check. I think...
        // Checks if there is available activity that can execute the intent.
        if (selectGPXFileIntent.resolveActivity(getPackageManager()) != null){
            startActivityForResult(Intent.createChooser(selectGPXFileIntent, "Upload Route via"), REQUEST_GPX_GET);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_GPX_GET && resultCode == RESULT_OK) {

            Uri selectedGPXUri = data.getData();
            InputStream inputStream = null;

            try{
                inputStream = getContentResolver().openInputStream(selectedGPXUri);
            } catch (FileNotFoundException e) {
                Log.e("DEBUGSYS", e.getMessage());
                return;
            }

            ActivityResultsRequestThread activityResultsRequestThread = new ActivityResultsRequestThread(inputStream, extractFileMetadata(selectedGPXUri), mainThreadHandler);
            activityResultsRequestThread.start();
        }
    }


    // Extracts file's name and size from URI and returns FileMetadata object containing the values.
    private FileMetadata extractFileMetadata(Uri gpxUri){
        Cursor cursor = getContentResolver().query(gpxUri, null, null, null, null, null);
        String displayName = "Unknown";
        long size = -1; // Unknown

        try{
            // moveToFirst() returns false if the cursor has 0 rows. Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()){
                int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                if (columnIndex != -1){
                    displayName = cursor.getString(columnIndex);
                }


                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                if (!cursor.isNull(sizeIndex)){
                    size = cursor.getLong(sizeIndex);
                }
                
            }
        }finally {
            cursor.close();
        }

        return new FileMetadata(displayName, size);
    }

    public void writeToFile(String filename, String text){
        File path = getApplicationContext().getFilesDir();
        try {
            FileOutputStream writer = new FileOutputStream(new File(path, filename));
            writer.write(text.getBytes());
            writer.close();
        } catch (IOException e) {
            Log.e("DEBUGSYS", e.getMessage());
        }
    }

    public String readFromFile(String filename){
        File path = getApplicationContext().getFilesDir();
        File readFrom = new File(path, filename);
        byte[] text = new byte[(int) readFrom.length()];

        try {
            FileInputStream stream = new FileInputStream(readFrom);
            stream.read(text);
            return new String(text);

        } catch (IOException e) {
            Log.e("DEBUGSYS", e.getMessage());
        }

        return "";
    }
}











