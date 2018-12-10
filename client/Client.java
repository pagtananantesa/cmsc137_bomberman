/*
 * GreetingClient.java
 * CMSC137 Sample Code for TCP Socket Programming
 */
import java.net.*;
import java.io.*;
import com.google.protobuf.*;

import java.lang.Iterable;

import proto.*;
import proto.TcpPacketProtos.TcpPacket;
import proto.TcpPacketProtos.TcpPacketOrBuilder;

import java.util.Scanner;
public class Client implements Runnable{
    Bombayah game;
    PlayerProtos.Player player;
    String serverAddress;
    int port;
    OutputStream outToServer;
    InputStream inFromServer;
    DataOutputStream out;
    DataInputStream in;
    Socket server;
    Scanner sc;
    Boolean isRunning;
    Thread clientThread;
    
    Listener listener;
    /////////////////////////////////////////////////////////////////////////////////////////////           Builders
    public Client(Bombayah game){
        this.connectToServer();
        this.game = game;
        this.isRunning=true;
    }
    private void buildPlayer(String uname){
        String username;
        this.player = null;
        System.out.print("Enter Player Name: ");
        this.player = PlayerProtos.Player.newBuilder()
                                    .setName(uname)
                                    .setId("-1")
                                    .build();

    }
    
///////////////////////////////////////////////////////////////////////////////////////////// 
    // public static void main(String [] args){                                        //Main Function / INIT
    //     Client client = new Client();
    //     client.connectToServer();
    //     client.mainMenu();
    // }
    // private void mainMenu(){                                                        //MAIN MENU
    //     Scanner sc = new Scanner(System.in);
    //     this.sc = sc;
    //     String lobbyId;
    //     int mainLoop = 10;
    //     do{
    //         System.out.println("Main Menu");
    //         System.out.println("#1 Create Lobby");
    //         System.out.println("#2 Connect to Lobby");
    //         System.out.println("#-1 EXIT");
    //         mainLoop = sc.nextInt();
    //         if(mainLoop==1){
    //             this.createLobby();
    //         }else if(mainLoop==2){
    //             System.out.print("Lobby Id: ");
    //             lobbyId = sc.next();
    //             this.joinLobby(lobbyId);
    //         }else{
    //             System.out.println("LOL");
    //         }
    //     }while(mainLoop!=-1);
    //     try{
    //         this.server.close();
    //     }catch(IOException e){
    //         e.printStackTrace();
    //         System.out.println("Cannot find (or disconnected from) Server");
    //     }   
    // }

/////////////////////////////////////////////////////////////////////////////////////////////THREADING
    public void start(){
        System.out.println("STARTED CLIENT THREAD");
        if(this.clientThread==null){
            this.isRunning = true;
            this.clientThread = new Thread(this,"Client");
            clientThread.start();
        }
    }
    public void run(){
        do{
            try{
                // System.out.println("CLIENT THREAD RUNNING");
                Thread.sleep(100);
            }catch(InterruptedException e){
                System.out.println("CLIENT");
                System.out.println(e);
            }
        }while(this.isRunning);
        System.out.println("CLIENT STOPPED");
        this.listener.stop();
    }                                                              

    public void stop(){
        this.isRunning = false;
    }

/////////////////////////////////////////////////////////////////////////////////////////////CONNECTION FUCNTIONS
    private void connectToServer(){
        this.serverAddress = "202.92.144.45";
        this.port = 80;
        try{
            this.server =  new Socket(this.serverAddress, this.port);
            System.out.println("Just connected to " + this.server.getRemoteSocketAddress());
            this.outToServer  = this.server.getOutputStream(); 
            this.inFromServer = this.server.getInputStream();
            this.out = new DataOutputStream(outToServer);
            this.in = new DataInputStream(inFromServer);
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Cannot find (or disconnected from) Server");
        }   
    }



    public String createLobby(){
        try{
            //Create Packet to send
            TcpPacket.CreateLobbyPacket sendPacket = TcpPacket.CreateLobbyPacket.newBuilder()
                                                .setType(TcpPacket.PacketType.CREATE_LOBBY)
                                                .setMaxPlayers(3)
                                                .build();

            // /* Send data to the ServerSocket */
            out.write(sendPacket.toByteArray());            //also convert packet to byteArray

            // /* Receive data from the ServerSocket */
            while(in.available()==0){       //Waiting
            }
            byte[] recievedPacketbytes = new byte[in.available()];          //Dynamic array size for recieving
            in.read(recievedPacketbytes);
            this.packetTypeChecker(recievedPacketbytes);

            //Add Dynamic Reciever for Error Catching


            TcpPacket.CreateLobbyPacket parsedRecievedPacket = TcpPacket.CreateLobbyPacket.parseFrom(recievedPacketbytes);
            System.out.println(parsedRecievedPacket.getLobbyId());

            //Might remove until here

            if(!parsedRecievedPacket.getLobbyId().startsWith("Cannot create lobby")){               //If there is an error during lobby creation
                // joinLobby(parsedRecievedPacket.getLobbyId());
                return parsedRecievedPacket.getLobbyId();
            }

        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Cannot find (or disconnected from) Server");
        }
        return "";
    }
    public void joinLobby(String lobbyId, String uname){
            //Also create player and register to server
        String message;             //for chat
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        try{
            //Build Packets
            buildPlayer(uname);
            sendConnect(lobbyId);
            // /* Receive data from the ServerSocket */
            while(this.in.available()==0){       //Waiting
            }
            byte[] recievedPacketbytes = new byte[this.in.available()];          //Dynamic array size for recieving
            // System.out.println("BEFORE RECIEVING JOIN PACKET");            
            this.in.read(recievedPacketbytes);
            // System.out.println("AFTER RECIEVING JOIN PACKET");

            if(packetTypeChecker(recievedPacketbytes)==1){          //PacketChecker returns 1 if there is an error during lobby join
                return;
            }
            this.listener = new Listener(this);                              //Listener INIT
            this.listener.start();
            this.game.setConnected();
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Cannot find (or disconnected from) Server");
        }
    }

    public int packetTypeChecker(byte[] bytePacket){
        TcpPacket.ChatPacket chat;
        TcpPacket.ConnectPacket connect;
        TcpPacket.DisconnectPacket disconnect;
        TcpPacket.PlayerListPacket playerlistpacket;
        TcpPacket.ErrLdnePacket errLdne;
        TcpPacket.ErrLfullPacket errLfull;
        TcpPacket.ErrPacket err;
        int i;
        try{

            TcpPacket packet = TcpPacket.parseFrom(bytePacket);
            
            if(packet.getType()==TcpPacket.PacketType.CHAT){
                chat = TcpPacket.ChatPacket.parseFrom(bytePacket);
                System.out.println(chat.getPlayer().getName()+": "+chat.getMessage());
                this.game.recieveChat(chat.getPlayer().getName()+": "+chat.getMessage());
            }else if(packet.getType()==TcpPacket.PacketType.CONNECT){
                connect = TcpPacket.ConnectPacket.parseFrom(bytePacket);
                this.game.recieveChat("Player "+connect.getPlayer().getName()+" has joined the lobby");
            }else if(packet.getType()==TcpPacket.PacketType.DISCONNECT){
                disconnect = TcpPacket.DisconnectPacket.parseFrom(bytePacket);
                if(disconnect.getUpdate()==TcpPacket.DisconnectPacket.Update.NORMAL){
                    this.game.recieveChat("Player "+disconnect.getPlayer().getName()+" has left the lobby");
                }else{
                    this.game.recieveChat("Player "+disconnect.getPlayer().getName()+" has been disconnected");
                }
                System.out.println(disconnect.getUpdate());
            }else if(packet.getType()==TcpPacket.PacketType.PLAYER_LIST){
                playerlistpacket = TcpPacket.PlayerListPacket.parseFrom(bytePacket);
                System.out.println("GOT PLAYERLIST PACKET");
                // for(String playerName:playerlistpacket.player_list){
                    System.out.println(playerlistpacket);
                    System.out.println("INSIDE PACKET");
                    System.out.println(playerlistpacket.getPlayerListCount());
                    System.out.println(playerlistpacket.getPlayerListList());
                // }
            }else if(packet.getType()==TcpPacket.PacketType.ERR_LDNE){
                System.out.println("THIS IS A ERR LDNE");
                errLdne = TcpPacket.ErrLdnePacket.parseFrom(bytePacket);
                System.out.println(errLdne.getErrMessage());
                return 1;
            }else if(packet.getType()==TcpPacket.PacketType.ERR_LFULL){
                System.out.println("THIS IS A ERR FULL LOBBY ERR");
                errLfull = TcpPacket.ErrLfullPacket.parseFrom(bytePacket);
                System.out.println(errLfull.getErrMessage());
                return 1;
            }else if(packet.getType()==TcpPacket.PacketType.ERR){
                System.out.println("DUNNO WHAT THIS ERROR IS");
                err =  TcpPacket.ErrPacket.parseFrom(bytePacket);
                System.out.println(err.getErrMessage());
                return 1;
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return 0;           //If 0 no errors
    }



///////////////////////////////////////////////////////////////////////////////////////////// PACKET SENDERS


    // Already In Lobby Commands
    public void sendDisconnect(){
        System.out.println("Disconnecting ...");
        try{
            TcpPacket.DisconnectPacket sendPacket = TcpPacket.DisconnectPacket.newBuilder()
                                                    .setType(TcpPacket.PacketType.DISCONNECT)
                                                    .setPlayer(this.player)
                                                    .setUpdate(TcpPacket.DisconnectPacket.Update.NORMAL)
                                                    .build();            
            this.out.write(sendPacket.toByteArray());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void sendChat(String message){
        try{
            TcpPacket.ChatPacket sendPacket = TcpPacket.ChatPacket.newBuilder()
                                                    .setType(TcpPacket.PacketType.CHAT)
                                                    .setPlayer(this.player)
                                                    .setMessage(message)
                                                    .build();
            this.out.write(sendPacket.toByteArray());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void sendPlayerList(){
        System.out.println("Loading Player List...");
        try{
            TcpPacket.PlayerListPacket sendPacket = TcpPacket.PlayerListPacket.newBuilder()
                                                    .setType(TcpPacket.PacketType.PLAYER_LIST)
                                                    .build();
            this.out.write(sendPacket.toByteArray());
        }catch(IOException e){
            e.printStackTrace();
        }
    }
    public void sendConnect(String lobbyId){
        TcpPacket.ConnectPacket sendPacket = TcpPacket.ConnectPacket.newBuilder()
                                                .setType(TcpPacket.PacketType.CONNECT)
                                                .setPlayer(this.player)
                                                .setLobbyId(lobbyId)
                                                .setUpdate(TcpPacket.ConnectPacket.Update.NEW)
                                                .build();
        System.out.println("BEFORE SENDING JOIN PACKET");
        try{
            this.out.write(sendPacket.toByteArray());
        }catch(IOException e){
            e.printStackTrace();
        }
        System.out.println("AFTER SENDING JOIN PACKET");
    }

}
