import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.Scanner;

public class Client {
	public static Socket client;
	static BufferedReader br,br1;
	static PrintWriter pw;
		
	public static void main(String[] args){
		try {
                        InetAddress addr;
                        int port;
                        if(args.length >= 2){
                            addr = InetAddress.getByName(args[0]);
                            port = Integer.parseInt(args[1]);
                       } else{
                            addr = InetAddress.getLocalHost();
                            port = 1028;
                        }
			client = new Socket(addr,port);
			br = new BufferedReader(new InputStreamReader(client.getInputStream()));
			br1 = new BufferedReader(new InputStreamReader(System.in));
			pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
			while(true) {
				String mess_client="";
				if(br1.ready()){
					mess_client = br1.readLine();
                                        pw.print(mess_client+"\n");
                                        pw.flush();
				}
				if(br.ready()) {
					String mess = br.readLine();
					System.out.println(mess);
					if(mess.equals("deconnexion"))
                                            break;
                                        if(mess.equals("Vous etes deconnecte")){
                                            pw.print("\n");
                                            pw.flush();
                                        }

                                }
                                
                                
				
			}
			pw.close();
			br.close();
			br1.close();
			client.close();
		}catch(Exception e) {
                    e.printStackTrace();
                    System.err.println("Connection impossible ");
                    
                }          
	}
	
    
}
