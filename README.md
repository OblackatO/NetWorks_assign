# Networks project    

The approach used to do this homework was : **Server -- Clients** Please, have a look at the following tree:  

```
src
|
|
|____Aquarium
|
|
|____Images
|
|
|____NetWorking 
```

[>]In the **package Aquarium** one is able to find everything related to the *items of the aquarium and GUI*.   
[>]In the **package Images** there are the *images* used for the aquarium items.  
[>]In the **package NetWorking** there are all the files related to the *Server and Clients*.  

## Description  
A server can have a maximum of 7 clients. When a client wants to connect to the server it will check if the server is alive,
if yes the client will send the server an association request. If the client can successfully be associated, it can send to the server the positions of its mobile items. When the latter happens, the server will send to all the other clients the received positions. The server does this constantly, and also verifies if the associated clients are still alive, concurrently.   

For performance reasons, and also for logical reasons, multi-threading is used in both the client and server sides.  
All the requests and responses exchanged between the clients and the server can be found in the Requests.java and in ResponseCodes.java.
Each aquarium can be seen as a client. Each client is considered by the server has a Client object that can be found in Client.java    

Most of the functions have a description.    

### Authors     
Gomes Pedro -- 017066611B  
Esteves Tiago --

### Copyright   
Copyright © 2018 Gomes Pedro and Tiago Esteves. All Rights Reserved.
