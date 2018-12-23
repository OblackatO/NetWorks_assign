package Networking;

public enum Requests {

	//sent by the clients to see if server is alive.
	HELO_REQUEST, 

	//sent by the clients to associate with the server.
    ASSOCIATION_REQUEST, 

    //sent by the clients to gracefully disconnect from the server.
    DISCONNECT_REQUEST, 

    //sent every time time a client sends positions of items 
    POSITIONS_REQUEST,

    //send by the server periodically, to all the clients, to keep track of 
    //which clients are yet alive.  
    IS_ALIVE; 
}
