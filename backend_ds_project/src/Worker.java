import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

public class Worker{

    public static void main(String[] args){

        Socket requestSocket = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        String address = args[0];

        try {
            requestSocket = new Socket(address, 5454);
            oos = new ObjectOutputStream(requestSocket.getOutputStream());
            ois = new ObjectInputStream(requestSocket.getInputStream());

            int i = 1;
            while (true){
                GPXPacket gpxPacket = (GPXPacket) ois.readObject();
                System.out.println("Worker " + args[0] + " Received packet #" + i);

                ArrayList<GPXPoint> gpxPoints = gpxPacket.getGpxPoints();
                System.out.println("List size: " + gpxPoints.size());
                for (GPXPoint p : gpxPoints){
                    System.out.println("lat= " + p.getLatitude() + " lon=" + p.getLongitude()
                            + " elev=" + p.getElevation() + " time=" + p.getTime());
                }

                MappingThread mappingThread = new MappingThread(oos, gpxPacket);
                mappingThread.start();

                i += 1;
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally{
            try{
                oos.close();
                ois.close();
                requestSocket.close();
            }catch (IOException e){
                e.printStackTrace();
            }

        }

    }
}









