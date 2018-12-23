package Networking;

public enum ResponseCodes {

	//Response to HELO_REQUEST
    OK_CODE,

    //Responses to ASSOCIATION_REQUEST
    CAN_ASSOCIATE,
	CANNOT_ASSOCIATE,
    
    //Sent to all clients from the server when a client disconnects,
    //sending a DISCONNECT_REQUEST
    DISCONNECTED,

    //Response to IS_ALIVE request
    YES_ALIVE;
}
