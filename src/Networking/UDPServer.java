package Networking;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
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
    Map<InetAddress, String> clients_IDs;
    Map<InetAddress, Integer> clients_map;
    Map<InetAddress, Integer> alive_clients;
    DatagramSocket server;

    public UDPServer(int port, String ip_address) throws UnknownHostException {

        //Inits map that contains info about the clients.
        this.clients_map = new HashMap<InetAddress, Integer>();
        this.alive_clients = new HashMap<InetAddress, Integer>();

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
            this.start();
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
            String[] message_splitted = message.split(this.TOKEN);
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleASSOCIATIONRequest(message_splitted[1], client_ip, client_port);
                }
            });
            thread1.start();

        }else if(message.contains(Requests.POSITIONS_REQUEST.toString())){

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
                    HandleDISCONNECTRequest(client_ip, client_port);
                }
            });
            thread1.start();

        }else if(message.contains(ResponseCodes.YES_ALIVE.toString())){
            this.HandleYESALIVEResponse(client_ip);

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

    private void HandleASSOCIATIONRequest(String clientID, InetAddress client_ip, int port){
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
                this.alive_clients.put(client_ip,0);
                this.clients_IDs.put(client_ip, clientID);
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

    private void HandleDISCONNECTRequest(InetAddress client_ip, int port){
        /**
         * Handles dissociation request, and tells others clients that some
         * client is going to be disconnected.
         */
        byte[] buffer = ResponseCodes.CAN_DISCONNECT.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
        this.SendMessage(packet);

        String message = client_ip.toString() + this.TOKEN;
        message += this.clients_IDs.get(client_ip) + this.TOKEN;
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
        this.alive_clients.remove(client_ip);
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

    private void IsClientAlive(){
        /**
         * Constantly checks if clients are still alive.
         *
         */
        while(true){

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            for (Map.Entry<InetAddress, Integer> entry : this.clients_map.entrySet()) {

                if (this.alive_clients.get(entry.getKey()) > 3) {
                    /**
                     * If value of client IP bigger than 3 in alive_clients map,
                     * means that the client does not answer for more than 15 seconds,
                     * and it will be considered dead, and hence removed from the the server.
                     * All the other clients will be warned as well.
                     */
                    this.alive_clients.remove(entry.getKey());
                    this.clients_map.remove(entry.getKey());
                    this.HandleDISCONNECTRequest(entry.getKey(), entry.getValue());

                } else {
                    int new_value = 1 + entry.getValue();
                    this.alive_clients.put(entry.getKey(), new_value);
                    byte[] buffer = Requests.IS_ALIVE.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer,
                                                                buffer.length,
                                                                entry.getKey(),
                                                                entry.getValue());
                    this.SendMessage(packet);
                }
            }

        }
    }

    private void HandleYESALIVEResponse(InetAddress client_ip){
        /**
         * Decrements the value of client_ip in this.alive_clients.
         * The decrementation makes sure that the server knows the client is alive.
         * Please, see the method: IsClientAlive for more details.
         */
        int new_value = this.alive_clients.get(client_ip) - 1;
        this.alive_clients.put(client_ip, new_value);
    }

    @Override
    public void run() {

        /**
         * Constantly checks if clients are alive, in
         * a separated thread.
         */
        Thread thread1 = new Thread(new Runnable() {
            @Override
            public void run() {
                IsClientAlive();
            }
        });
        thread1.start();

        /**
         * Constantly gets input from the clients(Aquariums).
         */
        while (true) {
            try {
                //System.out.println("Waiting for messages...");
                this.DissectMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
