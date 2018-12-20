package Aquarium;

import Networking.UDPServer;

import java.net.UnknownHostException;

public class Start {

    public static String SERVER_IP = "127.0.0.1";

    public static void main(String [] args){

        //Init Server
        try {
            UDPServer server = new UDPServer(0,Start.SERVER_IP);
            server.start();
        } catch (UnknownHostException e) {
            System.out.println("[>]An error occurred while init the server.");
            e.printStackTrace();
        }

        System.out.println("[>]Creating animation.");
        Animation animation_window = new Animation();
        animation_window.setSize(Aquarium.getcoordinateX(),Aquarium.getcoordinateY());
        animation_window.setVisible(true);

        //I was here : NEED to make documentation of functions in UDPServer and UDPClient


    }
}
