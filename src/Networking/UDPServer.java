package Networking;

import java.io.IOException;
import java.net.*;

public class UDPServer extends Thread {

    /** Class responsible to store the position of all items
     * of each aquarium client. It opens one thread per each client, in
     * order to handle requests
     */

    int port;
    InetAddress ip_address;

    DatagramSocket server;
    DatagramPacket packet = null;

    //input and output streams
    byte[] buffer=new byte[256];
    String outMessage=null;
    String inMessage=null;

    public UDPServer(int port, String ip_address) throws UnknownHostException {

        //Defines default port if needed.
        if(port == 0){
            this.port = 8080;
        }else{
            this.port = port;
        }

        //Defines default ip address to bind to(loop back interface)
        if(ip_address == null){
            InetAddress.getByName("127.0.0.1");
        }else{
            InetAddress.getByName(ip_address);
        }

        try {
            this.server = new DatagramSocket(this.port, this.ip_address);
            System.out.println("[>]Server is up and running.");
        } catch (SocketException e) {
            System.out.println("[>]An error occured while creating the server.");
            e.printStackTrace();
        }

    }

    public void DissectMessage() throws IOException {
        this.packet = new DatagramPacket(this.buffer, this.buffer.length);
        this.server.receive(this.packet);
        String message = new String(packet.getData(),0,this.buffer.length, "UTF-8");
        if (message.contains(Requests.HELO_REQUEST.toString())) { //Aquarium Server
            this.HandleHELORequest();
        }
    }

    private void HandleHELORequest(){
        System.out.println("[>]A new aquarium is up and running.");
        System.out.println("[>]Its IP address:"+this.packet.getAddress().toString());

        InetAddress address = this.packet.getAddress();
        int rep_port = this.packet.getPort();
        this.buffer = ResponseCodes.OK_CODE.toString().getBytes();
        this.packet = new DatagramPacket(this.buffer, this.buffer.length, address, rep_port);
        try {
            this.server.send(packet);
        } catch (IOException e) {
            System.out.println("[>]An error occurred while handling Discovery request");
            e.printStackTrace();
        }

    };

    private void HandlePositionsRequest(){

    };

    @Override
    public void run() {
        while (true) {
            try {
                this.DissectMessage();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
