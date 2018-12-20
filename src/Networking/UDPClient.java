package Networking;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;

public class UDPClient {

    /** Class responsible for handling for requesting all the positions
     * os all aquariums that are running. Furthermore, this class is also
     * responsible to update the position of the items of each Aquarium.
     */

    int server_port;
    InetAddress serverIP;

    DatagramSocket client;
    DatagramPacket packet = null;

    //input and output streams
    byte[] buffer=new byte[512];
    String inMessage;

    public UDPClient(int port, String ServerIP) throws UnknownHostException, SocketException, UnsupportedEncodingException {

        if(port == 0){
            this.server_port = 8080;
        }else{
            this.server_port = port;
        }

        this.client = new DatagramSocket();
        this.serverIP = InetAddress.getByName(ServerIP);

        //Checks if server is ready to communicate
        if(this.HELORequest()){
            System.out.println("[>]Server successfully responded. Possible to send position information.");
        }else{
            //System.out.println("Did not get any message.");
        }
    }

    private boolean HELORequest() throws UnsupportedEncodingException {
        /**Sends Discovery Requests to check if the
         * server is up and running.
         *
         * @return false if the server is not up and running,
         *         true otherwise.
         */
        this.ConvertRequests(Requests.HELO_REQUEST);
        this.packet = new DatagramPacket(this.buffer, this.buffer.length,
                                        this.serverIP,
                                        this.server_port);
        //Tries to send message
        try {
            this.client.send(packet);
        } catch (IOException e) {
            System.out.println("[>]There was a problem in the discovery Request.");
            e.printStackTrace();
        }
        System.out.println("Trying to receive message.");

        //Receives message
        this.packet = new DatagramPacket(this.buffer, this.buffer.length);
        try {
            this.client.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.inMessage = new String(packet.getData(), 0, packet.getLength(), "UTF-8");
        return this.inMessage.equals(ResponseCodes.OK_CODE.toString());


    }

    private void ConvertRequests(Requests request){
        /**Converts an instance of Requests to bytes, so it can be
         * properly sent over UDP.
         */
        this.buffer = request.toString().getBytes();
    }
}
