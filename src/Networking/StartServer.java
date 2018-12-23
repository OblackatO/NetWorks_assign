package Networking;

import Aquarium.Start;

import java.net.UnknownHostException;

public class StartServer {

    public static String SERVER_IP = null;
    public static int SERVER_PORT = 8000;

    public static void main(String [] args){

        if (args.length == 2) {
            SERVER_IP = args[0];
            SERVER_PORT = Integer.parseInt(args[1]);
        }

        try {
            UDPServer server = new UDPServer(StartServer.SERVER_PORT,StartServer.SERVER_IP);
            server.start();
        } catch (UnknownHostException e) {
            System.out.println("[>]An error occurred while init the server.");
            e.printStackTrace();
        }

    }

}
