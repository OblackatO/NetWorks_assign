package Networking;

import java.io.IOException;
import java.net.*;

public class UDPServer extends Thread {

    /** Class responsible to store the position of all items
     * of each aquarium client. It opens one thread per each client, in
     * order to handle requests
     */

    DatagramSocket server;
    String outMessage=null;
    String inMessage=null;

    public UDPServer(int port, String ip_address) throws UnknownHostException {

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
            System.out.println("[>]An error occured while creating the server.");
            e.printStackTrace();
        }
    }

    public void DissectMessage() throws IOException {
        byte[] buffer=new byte[512];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        this.server.receive(packet);
        String message = new String(packet.getData(),0,buffer.length, "UTF-8");
        InetAddress client_ip = packet.getAddress();
        int client_port = packet.getPort();
        if (message.contains(Requests.HELO_REQUEST.toString())) { //Aquarium Server
            this.HandleHELORequest(client_ip, client_port);
        }
    }

    private void HandleHELORequest(InetAddress client_ip, int port){
        System.out.println("[>]A new aquarium is up and running.");
        System.out.println("[>]Its IP address:"+client_ip.toString());

        byte[] buffer;
        DatagramPacket packet;
        buffer = ResponseCodes.OK_CODE.toString().getBytes();
        packet = new DatagramPacket(buffer, buffer.length, client_ip, port);
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
