package Aquarium;

import Networking.UDPServer;
import java.net.UnknownHostException;

public class Start {

    public static String SERVER_IP = null;
    public static int SERVER_PORT = 0;

    public static void main(String [] args){

        /*UNCOMMENT ME TO BE THE SERVER
        try {
            UDPServer server = new UDPServer(Start.SERVER_PORT,Start.SERVER_IP);
            server.start();
        } catch (UnknownHostException e) {
            System.out.println("[>]An error occurred while init the server.");
            e.printStackTrace();
        }
        */

        /*UNCOMMENT ME TO BE A CLIENT
        System.out.println("[>]Creating animation.");
        Animation animation_window = new Animation();
        animation_window.setSize(Aquarium.getcoordinateX(),Aquarium.getcoordinateY());
        animation_window.setVisible(true);
        */

    }
}