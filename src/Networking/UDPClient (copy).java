package Networking;

import Aquarium.Items.AquariumItem;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.util.UUID;


public class UDPClient extends Thread{

    /** Class responsible for handling for requesting all the positions
     * os all aquariums that are running. Furthermore, this class is also
     * responsible to update the position of the items of each Aquarium.
     */

    int server_port;
    InetAddress serverIP;
    DatagramSocket client;

    UUID uuid = UUID.randomUUID();
    String clientID = uuid.toString();
    final String TOKEN = "|";
    final int BUFFER_MAX = 100;

    //
    DatagramPacket[] dataBuffer;
    int in;
    int out;


    public UDPClient(int port, String ServerIP) throws UnknownHostException, SocketException{

        if(port == 0){
            this.server_port = 8080;
        }else{
            this.server_port = port;
        }

        this.client = new DatagramSocket();
        this.serverIP = InetAddress.getByName(ServerIP);

        DatagramPacket[] queuBuffer = new DatagramPacket[this.BUFFER_MAX];
        in = 0;
        out = 0;

        this.start();

    }

    private void HELORequest() {
        /**Sends Discovery Requests to check if the
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
        byte[] buffer = this.ConvertRequests(Requests.ASSOCIATION_REQUEST);
        String message = null;
        boolean isassociated = false;

        while (!(isassociated)) {

            message = this.RequestHandler(buffer);

            if (!(message.contains(ResponseCodes.CAN_ASSOCIATE.toString()))) {
                System.out.println("[>]Server reached maximum number of connections");
                System.out.println("[>]Trying again in a few seconds.");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                isassociated = true;
            }

        }
    }

    public boolean DISCONNECTRequest(){
        /**
         * Asks the server for a disconnection.
         *
         * @return true if disconnection successful, false otherwise.
         */

        byte[] buffer = this.ConvertRequests(Requests.DISCONNECT_REQUEST);
        String message = this.RequestHandler(buffer);
        return message.contains(ResponseCodes.CAN_DISCONNECT.toString());

    }

    private String RequestHandler(byte[] buffer){
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length,
                this.serverIP,
                this.server_port);
        //Tries to send message
        sendDatagram(packet);
        System.out.println("Trying to receive message.");
        packet = recieveDatagram();
        String message = null;
        try {
            message = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
            return message;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return message;
    }

    protected void sendDatagram(DatagramPacket packet){
        //Tries to send packet
        try {
            this.client.send(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected DatagramPacket recieveDatagram(){
        //Tries to send packet
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
         * Converts an instance of Requests to bytes, so it can be
         * properly sent over UDP.
         */
        return request.toString().getBytes();
    }

    private void SendFish(){
        sendDatagram(dataBuffer[out]);
        out = (++out) % this.BUFFER_MAX;
    }

    public void queueBuffer(AquariumItem item) {

        String message;

        message = this.clientID+this.TOKEN;
        message += item.getItemID()+this.TOKEN;
        message += item.getPosition().x+this.TOKEN;
        message += item.getPosition().y+this.TOKEN;



        byte[] buffer = message.getBytes();
        dataBuffer[in] = new DatagramPacket(buffer, buffer.length,
                this.serverIP,
                this.server_port);
        in = (++in) % this.BUFFER_MAX;

    }


    @Override
    public void run() {
        HELORequest(); //Checks if server is ready to communicate
        ASSOCIATIONRequest(); //Sends association request to server

    }

}
