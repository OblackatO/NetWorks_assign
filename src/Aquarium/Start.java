package Aquarium;

import Networking.UDPServer;
import java.net.UnknownHostException;

public class Start {

    public static String SERVER_IP = null;
    public static int SERVER_PORT = 8000;

    public static void main(String [] args){

        if (args.length == 2) {

            SERVER_IP = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
        }

        System.out.println("[>]Creating animation.");
        Animation animation_window = new Animation();
        animation_window.setSize(Aquarium.getcoordinateX(),Aquarium.getcoordinateY());
        animation_window.setVisible(true);


    }
}