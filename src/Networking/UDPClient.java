package Networking;

import Aquarium.Aquarium;
import Aquarium.Items.AquariumItem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.UUID;


public class UDPClient extends Thread{

    /** Class responsible for handling and requesting all the positions
     * of all aquariums that are running. Furthermore, this class is also
     * responsible to update the positions of the items of each Aquarium.
     */

    int server_port;
    InetAddress serverIP;
    DatagramSocket client;
    boolean isassociated = false;
    final Aquarium aquarium;

    UUID uuid = UUID.randomUUID();
    String clientID = uuid.toString();
    final String TOKEN = "@";
    final int BUFFER_MAX = 100;



    public UDPClient(int port, String ServerIP, Aquarium aquarium) throws UnknownHostException, SocketException{

        if(port == 0){
            this.server_port = 8080;
        }else{
            this.server_port = port;
        }

        this.client = new DatagramSocket();
        this.serverIP = InetAddress.getByName(ServerIP);

        this.aquarium = aquarium;

        this.start();

    }

    private void HELORequest() {
        /**Sends HELO Request to check if the
         * server is up and running.
         *
         * @return false if the server is not up and running,
         *         true otherwise.
         */
        byte[] buffer= this.ConvertRequests(Requests.HELO_REQUEST);
        String message = null;
        message = this.RequestHandler(buffer);
        if(message.contains(ResponseCodes.OK_CODE.toString())){
            System.out.println("[>]Server successfully responded. Possible to send position information.");
        }else{
            System.out.println("Did not get any message.");
        }
    }

    private void ASSOCIATIONRequest() {
        /**
         * Tries to assiciate with the server, so the client can start 
         * sending the positions of its items. If the server is full, the client 
         * will try to reconnect after 10 seconds, doing so consecutively either 
         * until the server accepts him, or the user closes the aquarium.
         */
        String message = Requests.ASSOCIATION_REQUEST.toString()+this.TOKEN;
        message += this.clientID+this.TOKEN;

        byte[] buffer = message.getBytes();


        while (!(this.isassociated)) {

            message = this.RequestHandler(buffer);

            if (!(message.contains(ResponseCodes.CAN_ASSOCIATE.toString()))) {
                System.out.println("[>]Server reached maximum number of connections");
                System.out.println("[>]Trying again in a few seconds.");
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                this.isassociated = true;
            }

        }
    }

    public boolean DISCONNECTRequest(){
        /**
         * Warns the server it is going to disconnect.
         *
         * @return true after warning the server of the disconnection.
         */

        String message = this.clientID + this.TOKEN;
        message += Requests.DISCONNECT_REQUEST.toString() + this.TOKEN;
        byte[] buffer = message.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                this.serverIP,
                this.server_port);
        this.sendDatagram(packet);
        return true;
    }

    private String RequestHandler(byte[] buffer){
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                this.serverIP,
                this.server_port);
        //Tries to send message
        sendDatagram(packet);
        packet = receiveDatagram();
        String message = null;
        try {
            message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            return message;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return message;
    }

    private void sendDatagram(DatagramPacket packet){
        //Tries to send packet
        try {
            this.client.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private DatagramPacket receiveDatagram(){
        //Tries to receive packet
        byte[] buffer = new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            this.client.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return packet;
    }

    private byte[] ConvertRequests(Requests request){
        /**
         * Converts an instance of Requests to bytes.
         */
        return request.toString().getBytes();
    }


    /*
    All methods handling received messages come here
     */

    private void messageHandler(){
        /**Reads all datagrams recieved by the client.
         * It then checks for the kind of message received and 
         * calls the handler for that message.
         */
        DatagramPacket packet = receiveDatagram();

        //Only process packets recieved from the server
        if(packet.getAddress() == serverIP) {
            String message = null;
            Thread thread = null;
            try {
                message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            if (message.contains(Requests.POSITIONS_REQUEST.toString())) {

                String[] tokenizedMessage = message.split(this.TOKEN);
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Received Fish.");
                        aquarium.updateExternalFish(tokenizedMessage[1],
                                                    tokenizedMessage[2],
                                                    Integer.parseInt(tokenizedMessage[3]),
                                                    Integer.parseInt(tokenizedMessage[4]));
                    }
                });
            }else if (message.contains(ResponseCodes.DISCONNECTED.toString())) {
                String[] tokenizedMessage = message.split(this.TOKEN);
                thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //TODO handle disconnected responses
                        aquarium.deleteExtFishFrom(tokenizedMessage[1]);
                    }
                });
            }
            thread.start();

        }

    }


    /*
    All methods handling sent messages come here
     */
    public void sendFish(AquariumItem item){
        if (isassociated) {
            String message;

            message = Requests.POSITIONS_REQUEST.toString() + this.TOKEN;
            message += this.clientID + this.TOKEN;
            message += item.getItemID() + this.TOKEN;
            message += item.getPosition().x + this.TOKEN;
            message += item.getPosition().y + this.TOKEN;

            byte[] buffer = message.getBytes();
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                    this.serverIP,
                    this.server_port);
            sendDatagram(packet);
        }
    }

    @Override
    public void run() {
        HELORequest(); //Checks if server is ready to communicate
        ASSOCIATIONRequest(); //Sends association request to server
        while(this.isassociated) {
            messageHandler();
        }

    }

}
