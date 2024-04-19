package com.example.ds_frontend;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;

import shared_classes.ActivitiesStats;

public class UserStatsRequestThread extends Thread{

    final int request_code = 1; // user stats request code.
    final String username;
    Handler handler;

    public UserStatsRequestThread(Handler handler, String username){
        this.handler = handler;
        this.username = username;
    }

    @Override
    public void run(){
        Socket requestSocket = null;
        DataOutputStream dos = null;
        ObjectInputStream ois = null;

        try {
            requestSocket = new Socket("10.0.2.2", 6969);
            dos = new DataOutputStream(requestSocket.getOutputStream());
            ois = new ObjectInputStream(requestSocket.getInputStream());

            sendRequest(dos);

            // Receive ActivitiesStats object containing user's total stats.
            ActivitiesStats userStats = (ActivitiesStats) ois.readObject();

            // Receive ActivitiesStats object containing global user stats.
            ActivitiesStats globalStats = (ActivitiesStats) ois.readObject();

            // Send MeanStats objects to Handler using bundle.
            Bundle bundle = new Bundle();
            bundle.putSerializable("USER_STATS", userStats);
            bundle.putSerializable("GLOBAL_STATS", globalStats);

            Message msg = Message.obtain();
            msg.setData(bundle);
            handler.sendMessage(msg);


        } catch (IOException | ClassNotFoundException e) {
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

    private void sendRequest(DataOutputStream dos) throws IOException {

        dos.writeInt(request_code);
        dos.writeUTF(username);
    }
}
