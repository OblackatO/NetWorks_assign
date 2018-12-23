package Networking;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ConcurrentSkipListSet;

public class UDPServer extends Thread {

    /** Class responsible to store the position of all items
     * of each aquarium client. It opens one thread per each client, in
     * order to handle requests
     */
    final String TOKEN = "@";
    final int MAX_CLIENTS = 7;
    int CURRENT_CLIENTS = 0;
    DatagramSocket server;

    //The data type ConcurrentSkipListSet avoids problems resulting in
    //concurrent threads accessing clients, at the same time. This behavior
    // is needed here.
    ConcurrentSkipListSet<Client> clients;

    public UDPServer(int port, String ip_address) throws UnknownHostException {

        //Inits arraylist that contains info about the clients.
        this.clients = new ConcurrentSkipListSet<Client>();

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
        this.server.receive(packet);
        String message = new String(packet.getData(), 0, packet.getLength());
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

            String[] message_splitted = message.split(this.TOKEN);
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleDISCONNECTRequest(client_ip, message_splitted[0], client_port);
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

            if((Client.ALL_IDs == null) || !(Client.ALL_IDs.contains(clientID))){
                System.out.println("This is the client ID:"+clientID);
                Client new_client = new Client(client_ip, clientID, port);
                this.clients.add(new_client);
                buffer = ResponseCodes.CAN_ASSOCIATE.toString().getBytes();
            }else{
                byte[] message = "Already associated".getBytes();
                buffer = message;
            }
        }

        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
        this.SendMessage(packet);
    }

    private void HandleItemsPositions(InetAddress client_ip,  DatagramPacket positions){
        /**
         * Sends the position of some items from some client, to all the other clients
         * except the one sending its own items' positions.
         */

        for(Client client: this.clients){

            if(client.ip_address.toString().equals(client_ip.toString())){
                //Skips the client sending the positions
                continue;
            }

            positions.setAddress(client.ip_address);
            positions.setPort(client.port);

            this.SendMessage(positions);
        }
    }

    private void HandleDISCONNECTRequest(InetAddress client_ip, String clientID, int port){
        /**
         * Handles a dissociation request, and tells others clients that some
         * client is going to be disconnected.
         */
        //TODO is this still needed?
        byte[] buffer = ResponseCodes.CAN_DISCONNECT.toString().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
        this.SendMessage(packet);

        Client client = null;
        for(Client client_sec: this.clients){
            if((client_sec.ip_address.toString().equals(client_ip.toString())) &&
                client_sec.ID.equals(clientID)){
                    client = client_sec;
                    break;
            }
        }
        String message = ResponseCodes.DISCONNECTED.toString() + this.TOKEN;
        message += client.ID + this.TOKEN;

        buffer = message.getBytes();
        packet = new DatagramPacket(buffer, buffer.length);

        for(Client client_sec: this.clients){

            if(client_sec.ip_address.toString().equals(client_ip.toString())){
                continue;
            }

            packet.setAddress(client.ip_address);
            packet.setPort(client.port);

            this.SendMessage(packet);
        }

        //removes client from the arraylist of clients
        this.clients.remove(client);

        this.CURRENT_CLIENTS--;
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
         * Constantly checks if clients are still alive, every 5 seconds.
         *
         */
        while(true){

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            System.out.println("Checking fishes..");
            for(Client client: this.clients){
                /**
                 * If value of total_YESALIVE_ANSWERS is bigger than 3 that
                 * means that the client does not answer for +/- 15 seconds,
                 * and it will be considered dead, and hence removed from the the server.
                 * All the other clients will be warned as well.
                 */
                if(client.total_YESALIVE_ANSWERS > 3){
                    this.clients.remove(client);
                }else{
                    client.total_YESALIVE_ANSWERS++;
                    byte[] buffer = Requests.IS_ALIVE.toString().getBytes();
                    DatagramPacket packet = new DatagramPacket(buffer,
                            buffer.length,
                            client.ip_address,
                            client.port);
                    this.SendMessage(packet);
                }
            }
        }
    }

    private void HandleYESALIVEResponse(InetAddress client_ip){
        /**
         * Decrements the value of total_YESALIVE_ANSWERS
         * The decrementation makes sure that the server knows the client is alive.
         * Please, see the method: IsClientAlive() for more details.
         */

        for(Client client: this.clients){
            if(client.ip_address.toString().equals(client_ip.toString())){
                client.total_YESALIVE_ANSWERS--;
                break;
            }
        }
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