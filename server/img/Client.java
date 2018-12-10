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
public class Client{
    PlayerProtos.Player player;
    String serverAddress;
    int port;
    OutputStream outToServer;
    InputStream inFromServer;
    DataOutputStream out;
    DataInputStream in;
    Socket server;
    Scanner sc;

    public static void main(String [] args){
        Client client = new Client();
        client.connectToServer();
        client.mainMenu();
    }
    private void mainMenu(){
        Scanner sc = new Scanner(System.in);
        this.sc = sc;
        String lobbyId;
        int mainLoop = 10;
        do{
            System.out.println("Main Menu");
            System.out.println("#1 Create Lobby");
            System.out.println("#2 Connect to Lobby");
            mainLoop = sc.nextInt();
            if(mainLoop==1){
                this.createLobby();
            }else if(mainLoop==2){
                System.out.print("Lobby Id: ");
                lobbyId = sc.next();
                this.joinLobby(lobbyId);
            }else{
                System.out.println("LOL");
            }
        }while(mainLoop!=-1);
        try{
            this.server.close();
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Cannot find (or disconnected from) Server");
        }   
    }
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
    private void createLobby(){
        try{
            //Create Packet to send
            TcpPacket.CreateLobbyPacket sendPacket = TcpPacket.CreateLobbyPacket.newBuilder()
                                                .setType(TcpPacket.PacketType.CREATE_LOBBY)
                                                // .setLobbyId("aa")
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
            TcpPacket.CreateLobbyPacket parsedRecievedPacket = TcpPacket.CreateLobbyPacket.parseFrom(recievedPacketbytes);
            System.out.println(parsedRecievedPacket.getLobbyId());
			// //closing the socket of the client
            // server.close();
            if(!parsedRecievedPacket.getLobbyId().startsWith("Cannot create lobby")){
                joinLobby(parsedRecievedPacket.getLobbyId());
            }
        }catch(IOException e){
            e.printStackTrace();
            System.out.println("Cannot find (or disconnected from) Server");
        }
    }
    private void joinLobby(String lobbyId){
            //Also create player and register to server
        Listener listener;
        String message;             //for chat
        try{
            //Build Packets
            buildPlayer();
            // this.player = null;
            // System.out.print("Enter Player Name: ");
            // username = sc.next();
            // this.player = PlayerProtos.Player.newBuilder()
            //                             .setName(username)
            //                             .setId("-1")
            //                             .build();
            sendConnect(lobbyId);
            // TcpPacket.ConnectPacket sendPacket = TcpPacket.ConnectPacket.newBuilder()
            //                                     .setType(TcpPacket.PacketType.CONNECT)
            //                                     .setPlayer(this.player)
            //                                     .setLobbyId(lobbyId)
            //                                     .setUpdate(TcpPacket.ConnectPacket.Update.NEW)
            //                                     .build();
            //Send Packets

            // /* Receive data from the ServerSocket */
            while(this.in.available()==0){       //Waiting
            }
            byte[] recievedPacketbytes = new byte[this.in.available()];          //Dynamic array size for recieving
            System.out.println("BEFORE RECIEVING JOIN PACKET");
            
            this.in.read(recievedPacketbytes);
            
            // this.packetTypeChecker(recievedPacketbytes);
            System.out.println("AFTER RECIEVING JOIN PACKET");

            

            // Add PacketListeners for Successful Connection, Lobby Full Error, and No Lobby Exists Error


            if(packetTypeChecker(recievedPacketbytes)==1){
                return;
            }

            // TcpPacket.ConnectPacket recievedPacket = TcpPacket.ConnectPacket.parseFrom(recievedPacketbytes);
            // System.out.println(recievedPacket.getLobbyId());
            // System.out.println(recievedPacket.getPlayer().getName());
            // System.out.println(recievedPacket.getPlayer().getId());
            // System.out.println("Player " + recievedPacket.getPlayer().getName() + " has joined the lobby");
            listener = new Listener(this);
            listener.start();
            do{
                message = sc.next();
                // System.out.print(recievedPacket.getPlayer().getName()+": ");
                System.out.print(this.player.getName()+": ");
                if(message.equals("out")){
                    sendDisconnect();
                    break;
                }else if(message.equals("")){
                }else if(message.equals("ListPlayer")){
                    sendPlayerList();
                }else{
                    sendChat(message);
                }
                System.out.flush();
            }while(true);
            listener.stop();          //terminate listener
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
        // System.out.println("packetChecker");
        try{

            TcpPacket packet = TcpPacket.parseFrom(bytePacket);
            // System.out.println(packet.getType());
            
            if(packet.getType()==TcpPacket.PacketType.CHAT){
                // System.out.println("IN_CHAT");
                chat = TcpPacket.ChatPacket.parseFrom(bytePacket);
                // System.out.println(chat.getPlayer().getName()+"mine: "+chat.getMessage());
                System.out.println(chat.getMessage());
                // System.out.println("OUT_CHAT");
            }else if(packet.getType()==TcpPacket.PacketType.CONNECT){
                // System.out.println("IN_CONNECT");
                connect = TcpPacket.ConnectPacket.parseFrom(bytePacket);
                System.out.println("Player "+connect.getPlayer().getName()+" has joined the lobby");
                // System.out.println("OUT_CONNECT");
            }else if(packet.getType()==TcpPacket.PacketType.DISCONNECT){
                // System.out.println("IN_DISCONNECT");
                disconnect = TcpPacket.DisconnectPacket.parseFrom(bytePacket);
                System.out.println("Player "+disconnect.getPlayer().getName()+" has left the lobby");
                // System.out.println(disconnect.getUpdate());
                // System.out.println("OUT_DISCONNECT");
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





    // Already In Lobby Commands
    private void sendDisconnect(){
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
    private void sendChat(String message){
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
    private void sendPlayerList(){
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
    private void sendConnect(String lobbyId){
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
    private void buildPlayer(){
        String username;
        this.player = null;
        System.out.print("Enter Player Name: ");
        username = this.sc.next();
        this.player = PlayerProtos.Player.newBuilder()
                                    .setName(username)
                                    .setId("-1")
                                    .build();

    }
}
