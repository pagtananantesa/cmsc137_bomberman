import java.net.*;
import java.io.*;
import com.google.protobuf.*;
import proto.*;
import proto.TcpPacketProtos.TcpPacket;
import java.util.*;
// import bombayah.Bombayah;
public class Listener implements Runnable{
    public Thread clockThread = null;
    public Client client = null;
    public DataInputStream in = null;
    private boolean isRunning;
    public Listener(Client client){
        this.client = client;
    }
    public void setClient(Client client){
        this.client = client;
        this.in = this.client.in;
    }
    public void start(){
        if(this.clockThread == null){
            this.isRunning = true;
            clockThread = new Thread(this,"Listener");
            clockThread.start();
        }
    }
    public void run() {
            do{
                try {
                    listenForPackets();
                    // System.out.println("listening for packets");
                    Thread.sleep(10);
                } catch (InterruptedException e){
                    System.out.println("LISTENER");
                    System.out.println(e);    
                }
            }while (isRunning);
            System.out.println("Listener Stopped");
    
    }
    public void stop(){
        this.isRunning = false;
    }
    public void listenForPackets(){             //only active in the lobby
        // System.out.println("Starting Listen for packets");
        try{
            DataInputStream inputStream = this.client.in;
            while(inputStream.available()==0 && this.isRunning){       //Waiting
            }
            if(!this.isRunning){
                return;
            }


            byte[] recievedPacketbytes = new byte[inputStream.available()];          //Dynamic array size for recieving
            inputStream.read(recievedPacketbytes);
            
            client.packetTypeChecker(recievedPacketbytes);           
        }catch(IOException e){
            // e.printStackTrace();
            System.out.println("Cannot find (or disconnected from) Server");
        }
        // System.out.println("Closing Listen for packets");

    }
}