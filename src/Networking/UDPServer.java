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

    //The collection ConcurrentSkipListSet avoids problems resulting in
    //concurrent threads accessing clients, at the same time. This behavior
    // is needed here.
    ConcurrentSkipListSet<Client> clients;

    public UDPServer(int port, String ip_address) throws UnknownHostException {

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
            System.out.println("Disconnect request recieved.");
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleDISCONNECTRequest(client_ip, message_splitted[1], client_port);
                }
            });
            thread1.start();

        }else if(message.contains(ResponseCodes.YES_ALIVE.toString())){

            String[] message_splitted = message.split(this.TOKEN);
            Thread thread1 = new Thread(new Runnable() {
                @Override
                public void run() {
                    HandleYESALIVEResponse(message_splitted[1]);
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
        String message = new String(positions.getData(), 0, positions.getLength());
        String[] message_splitted = message.split(this.TOKEN);
        for(Client client: this.clients){

            if(client.ID.equals(message_splitted[1])){
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
        Client disconnectingClient = null;
        String message = null;
        for(Client client_sec: this.clients){
            if(client_sec.ID.equals(clientID)){
                    disconnectingClient = client_sec;
                message = ResponseCodes.DISCONNECTED.toString() + this.TOKEN;
                message += disconnectingClient.ID + this.TOKEN;
                    break;
            }
        }

        if(disconnectingClient != null){

            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);

            for(Client client_sec: this.clients){

                if(client_sec == disconnectingClient){
                    //Skips the client sending the DISCONNECT Request
                    continue;
                }

                packet.setAddress(client_sec.ip_address);
                packet.setPort(client_sec.port);

                this.SendMessage(packet);
            }

            //removes client from the arraylist of clients
            this.clients.remove(disconnectingClient);
            Client.ALL_IDs.remove(disconnectingClient.ID);
            this.CURRENT_CLIENTS--;
        }
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

            for(Client client: this.clients){
                /**
                 * If value of total_YESALIVE_ANSWERS is bigger than 2 that
                 * means that the client does not answer for +/- 15 seconds,
                 * and it will be considered dead, and hence removed from the the server.
                 * All the other clients will be warned as well.
                 */
                if(client.total_YESALIVE_ANSWERS > 2){
                    this.clients.remove(client);
                    //TODO send DISCONNECT messages to other clients
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

    private void HandleYESALIVEResponse(String clientID){
        /**
         * Resets the value of total_YESALIVE_ANSWERS
         * The variable makes sure that the server knows the client is alive.
         * Please, see the method: IsClientAlive() for more details.
         */

        for(Client client: this.clients){
            if(client.ID.equals(clientID)){
                client.total_YESALIVE_ANSWERS = 0;
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