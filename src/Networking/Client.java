package Networking;

import java.net.InetAddress;
import java.util.ArrayList;

public class Client {
    /**
     * Information needed to deal with a client.
     *
     * Each client has a different ID on top of its IP in case
     * some clients(aquariums) are running on the same network card with the same IP address.
     */

    public InetAddress ip_address;
    public String ID;
    public int port;
    public int total_YESALIVE_ANSWERS = 0;

    public static ArrayList<String> ALL_IDs = null; //keeps track of all clients' IDs

    public Client(InetAddress ip_address, String ID, int port){

        this.ip_address = ip_address;
        this.ID = ID;
        this.port = port;

        if(Client.ALL_IDs == null){
            Client.ALL_IDs = new ArrayList<String>();
        }
    }
}
