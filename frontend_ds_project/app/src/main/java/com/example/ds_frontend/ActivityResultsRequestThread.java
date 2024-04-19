package com.example.ds_frontend;
import shared_classes.ActivityResults;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.Socket;


public class ActivityResultsRequestThread extends Thread{

    final int request_code = 0; // activity results request code.
    InputStream inputStream;
    FileMetadata fileMetadata;
    Handler handler;

    public ActivityResultsRequestThread(InputStream inputStream, FileMetadata fileMetadata, Handler handler){
        this.inputStream = inputStream;
        this.fileMetadata = fileMetadata;
        this.handler = handler;
    }

    @Override
    public void run(){
        Socket requestSocket = null;
        DataOutputStream dos = null;
        ObjectInputStream ois = null;

        try{
            Log.e("DEBUGSYS", "Trying to connect to server...");
            requestSocket = new Socket("10.0.2.2", 6969);
            Log.e("DEBUGSYS", "Connection to server succesful");
            dos = new DataOutputStream(requestSocket.getOutputStream());
            ois = new ObjectInputStream(requestSocket.getInputStream());

            // Send request code first.
            dos.writeInt(request_code);
            dos.flush();
            sendFile(inputStream, fileMetadata, dos); // Send file.

            // Receive ActivityResults object containing results.
            ActivityResults activityResults = (ActivityResults) ois.readObject();

            /* FOR TESTING PURPOSES LOG RESULTS */
            String results = activityResults.getResults();
            Log.e("DEBUGSYS", results);

            // Send ActivityResults object to Handler.
            Message msg = Message.obtain();
            msg.obj = activityResults;
            handler.sendMessage(msg);

        }catch (IOException | ClassNotFoundException e){
            Log.e("DEBUGSYS", e.getMessage());
        } finally {
            try{
                dos.close();
                ois.close();
                requestSocket.close();
            }catch (IOException | NullPointerException e){
                Log.e("DEBUGSYS", e.getMessage());
            }
        }
    }

    private void sendFile(InputStream input, FileMetadata fileMetadata, DataOutputStream dos) throws IOException {
        dos.writeLong(fileMetadata.getSize()); // send file size.
        dos.writeUTF(fileMetadata.getName()); // send file name.

        // Send file contents.
        int read = 0;
        while ((read = input.read()) != -1){
            dos.writeByte(read);
        }
        dos.flush();
        input.close();
    }
}
