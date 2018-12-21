package Networking;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class UDPServer extends Thread {

    /** Class responsible to store the position of all items
     * of each aquarium client. It opens one thread per each client, in
     * order to handle requests
     */

    final String TOKEN = "|";
    final int MAX_CLIENTS = 7;
    int CURRENT_CLIENTS = 0;
    Map<InetAddress, Integer> clients_map;
    DatagramSocket server;

    public UDPServer(int port, String ip_address) throws UnknownHostException {

        //Inits map that contains info about the clients.
        this.clients_map = new HashMap<InetAddress, Integer>();

        //Defines default port if needed.
        if(port == 0){
            port = 8080;
        }else{
            port = port;
        }

        //Defines default ip address to bind to(loop back interface)
        InetAddress ip_net_address;
        if(ip_address == null){
            ip_net_address = InetAddress.getByName("127.0.0.1");
        }else{
            ip_net_address = InetAddress.getByName(ip_address);
        }

        try {
            this.server = new DatagramSocket(port, ip_net_address);
            System.out.println("[>]Server is up and running.");
            System.out.println("Server Ip address:"+this.server.getLocalAddress());
            System.out.println("Server port:"+this.server.getLocalPort());
        } catch (SocketException e) {
            System.out.println("[>]An error occurred while creating the server.");
            e.printStackTrace();
        }
    }

    public void DissectMessage() throws IOException {
        /**
         * Dissects a request, or message of the client.
         * Creates a new thread to handle each request.
         *
         * Because a new thread is created to every specific request/message, this function can be constantly
         * called in the run() function of this class itself.
         */
        byte[] buffer=new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        System.out.println("Waiting for packet.");
        this.server.receive(packet);
        String message = new String(packet.getData(),0,buffer.length);
        InetAddress client_ip = packet.getAddress();
        int client_port = packet.getPort();

        if (message.contains(Requests.HELO_REQUEST.toString())) {

            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleHELORequest(client_ip, client_port);
                }
            });
            thread1.start();

        }else if(message.contains(Requests.ASSOCIATION_REQUEST.toString())){

            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleASSOCIATIONRequest(client_ip, client_port);
                }
            });
            thread1.start();

        }else if(message.contains(Requests.GETPOSITIONS_REQUEST.toString())){

            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleItemsPositions(client_ip, packet);
                }
            });
            thread1.start();

        }else if(message.contains(Requests.DISCONNECT_REQUEST.toString())){

            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleDISSOCIATIONRequest(client_ip, client_port);
                }
            });
            thread1.start();

        }else{
            System.out.println(String.format("Message: %s from client: %s not understood.", message, client_ip.toString()));
        }
    }

    private void HandleHELORequest(InetAddress client_ip, int port){
        /**
         * Handles HELORequest from client.
         */

        System.out.println("[>]A new aquarium is up and running.");
        System.out.println("[>]IP address:Port -- "+client_ip.toString()+":"+ String.valueOf(port));

        byte[] buffer = ResponseCodes.OK_CODE.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
        this.SendMessage(packet);

    };

    private void HandleASSOCIATIONRequest(InetAddress client_ip, int port){
        /**
         * Handles ASSOCIATION_request from the client. If the number of maximum clients that
         * can be connected to the server has been reached, a CANNOT_ASSOCIATE
         * response will be sent to the client.
         */

        byte[] buffer;
        if(++this.CURRENT_CLIENTS == this.MAX_CLIENTS){
            System.out.println("[>]Maximum clients has been reached. A connection has been refused.");
            buffer = ResponseCodes.CANNOT_ASSOCIATE.toString().getBytes();

        }else{
            if(!(this.clients_map.containsKey(client_ip))){
                this.clients_map.put(client_ip, port);
            }
            buffer = ResponseCodes.CAN_ASSOCIATE.toString().getBytes();
        }
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
        this.SendMessage(packet);
    }

    private void HandleItemsPositions(InetAddress client_ip,  DatagramPacket positions){
        /**
         * Sends the position of some items from some client, to all the other clients
         * except the one sending its own items' positions.
         */

        for (Map.Entry<InetAddress, Integer> entry : this.clients_map.entrySet()) {

            String entry_ip = entry.getKey().toString();
            if(entry_ip.equals(client_ip.toString())){
                continue;
            }

            //sets destination address+port, to the one of the client in the current iteration.
            positions.setAddress(entry.getKey());
            positions.setPort(entry.getValue());

            this.SendMessage(positions);
        }
    }

    private void HandleDISSOCIATIONRequest(InetAddress client_ip, int port){
        /**
         * Handles dissociation request, and tells others clients that some
         * client is going to be disconnected.
         */
        byte[] buffer = ResponseCodes.CAN_DISCONNECT.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
        this.SendMessage(packet);

        String message = client_ip.toString() + this.TOKEN;
        message += ResponseCodes.DISCONNECTED.toString() + this.TOKEN;

        buffer = message.getBytes();
        packet = new DatagramPacket(buffer, buffer.length);
        for (Map.Entry<InetAddress, Integer> entry : this.clients_map.entrySet()) {

            String entry_ip = entry.getKey().toString();
            if(entry_ip.equals(client_ip.toString())){
                continue;
            }

            //sets destination address+port, to the one of the client in the current iteration.
            packet.setAddress(entry.getKey());
            packet.setPort(entry.getValue());

            this.SendMessage(packet);
        }

        //removes client from the map of clients
        this.clients_map.remove(client_ip);
    }

    private void SendMessage(DatagramPacket packet){
        /**
         * Send a message through the server socket.
         */
        try {
            this.server.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        /**
         * Constantly gets input from the clients(Aquariums).
         */
        while (true) {
            try {
                System.out.println("Waiting for messages...");
                this.DissectMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
