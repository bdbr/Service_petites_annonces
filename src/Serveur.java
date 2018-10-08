import java.net.*;
import java.io.*;

public class Serveur{

    
    public static void main(String args[]){
        try{
            Donnee  donnee = new Donnee();
            ServerSocket server=new ServerSocket(1028);
            while(true){
                 Socket socket;
                 socket=server.accept();
                 ServeurThread st = new ServeurThread(socket,donnee);
                 donnee.addClient(st);
                 st.start();
	         
            }
        
        }catch(Exception e){
            e.printStackTrace();
        }
    }
    
    
}