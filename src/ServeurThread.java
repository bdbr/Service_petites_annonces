import java.net.*;
import java.io.*;
import java.util.*;

public class ServeurThread extends Thread{ 
    private static int compteur_id = 0;
    private Socket client;
    private Donnee donnee;
    private int id;
    private PrintWriter pw;
    private BufferedReader br;
    private boolean availableClient;
    private int targetClient;
    private boolean waitingResponse;
    private String response;
    private boolean connexionCC;
    private boolean ok = true;
    
    public ServeurThread(Socket client,Donnee donnee){
        this.client = client;
        this.donnee = donnee;
        id = compteur_id++;
        availableClient = true;
        targetClient = -1;
        waitingResponse = false;
        connexionCC = false;
    }
    public synchronized void setConnexionCC(boolean b){
        connexionCC = b;
    }
    public synchronized boolean getConnexionCC(){
        return connexionCC;
    }
    public void setWaitingResponse(boolean b){
        waitingResponse = b;
    }
    
    public void setResponse(String s){
        response =  s;
    }
    
    public int getSTId(){
        return id;
    }
    public int getTargetClient(){
        return targetClient;
    }
    
    public boolean isAvailable(){
        return availableClient;
    }
   
    
    public void run(){
        String str = "",enTete = "";
        
        Runtime.getRuntime().addShutdownHook(new Thread() {
        public void run() {
		try {
			Thread.sleep(200);
                        pw.print("deconnexion");
                    	pw.flush();
                        ok = false;


                } catch (InterruptedException e) {
				                
			e.printStackTrace();
		}
        }
        });
        
        try{
            pw = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(client.getInputStream()));
            pw.print("hello!\n");
            pw.flush();
            while(ok){
                str = br.readLine();
                if(str.length() > 3){
                    enTete = str.substring(0,4);
                    if(enTete.equals("QUIT")){
                        pw.print("deconnexion");
                        pw.flush();
                        break;
                    }
                    switch(enTete){
                        case "POST":
                            postRequest(str);
                            break;
                        case "ANNO":
                            annonceRequest(str);
                            break;
                        case "SUPR":
                            suprRequest(str);
                            break;
                        case "OKAY":
                            recqRequest(str);
                            break;
                        case "NONE":
                            recqRequest(str);
                            break;
                        default: 
                            sendToClient("Mauvaise commande.");
                            break;
                    
                    }
                }

            }
            
            
        
        }catch(Exception e){
//             e.printStackTrace();
            
        }finally{
            try{
                pw.flush();
                pw.close();
                br.close();
                donnee.delClientMessage(id);
                donnee.delClient(this);
                client.close();
            }catch(Exception e){
//                 e.printStackTrace();
            }
        }
    }
    
    public void annonceRequest(String msg){
        String tab[] = msg.split("\\s+");
        if(tab.length < 2){
            pw.print("Commande introuvable.\n");
            pw.flush();
            return;
        }
        switch(tab[1]) {
            case "-a":
                for(Message m: donnee.getMessages()){
                    pw.print(m.getId()+" "+m.getCorps()+"\n\n");
                    pw.flush();
                } 
                pw.printf("FIN DES ANNONCES\n");
                pw.flush();
                break;
            case "-n":
                try{
                    this.availableClient = false;
                    int id_msg = Integer.parseInt(tab[2]);

                    ServeurThread st =  donnee.getAuthor(id_msg);
                    if(st==null ||st.getSTId()==getSTId()){
                        pw.print("NONE\n");
                        pw.flush();
                        this.availableClient = true;
                        break;
                    }
                    if(!st.isAvailable()){
                         sendToClient("client occupe.\n");
                         this.availableClient = true;
                         break;
                     }
                    st.sendToClient("REQC "+id_msg +" "+id);
                    this.targetClient = st.getSTId();
                    pw.print("REQC en cours\n");
                    pw.flush();
                    waitingResponse = true;
                    long startTime = System.currentTimeMillis();
                    long endTime =  startTime + 30000;
                    while(waitingResponse && System.currentTimeMillis() < endTime){sleep(500);}
                    if(response.equals("OKAY")){
                        waitingResponse = false;
                        clientToClient(this.targetClient);
                    } else if(response.equals("NONE")||waitingResponse){
                        waitingResponse = false;
                        pw.print("Connexion non etablie\n");
                        pw.flush();
                        this.targetClient = -1;
                    }
                        

                 }catch(Exception e){
//                      e.printStackTrace();
                    if(e instanceof java.lang.NumberFormatException){
                        pw.print("id non valide\n");
                        pw.flush();
                    }
                 }
                break;
            case "-m":
                for(Message m: donnee.getClientMessages(id)){
                    pw.print(m.getId()+" "+m.getCorps()+"\n\n");
                    pw.flush();
                } 
                pw.printf("FIN DES ANNONCES\n");
                pw.flush();
                break;
            default:
                sendToClient("Mauvaise commmande les options disponibles pour ANNO sont:");
                sendToClient("ANNO -a  pour voir toutes les annonces"); 
                sendToClient("ANNO -m  pour voir toutes vos annonces"); 
                sendToClient("ANNO -n [id_annonce] pour discuter avec le posteur de annonce"); 
                break;
        }
        
    }
    
    public void suprRequest(String msg){
        String tab[] = msg.split("\\s+");
        if(tab.length < 2){
            pw.print("Commande introuvable.\n");
            pw.flush();
            return;
        }
        switch(tab[1]){
            case "-a":
            donnee.delClientMessage(id);
            pw.print("Tous les Message ont bien ete supprime\n");
            pw.flush();
            break;
            case "-n":
                try{
                    int id_msg = Integer.parseInt(tab[2]);
                    if(donnee.delMessage(id_msg,id)){
                        pw.print("Message "+id_msg+" supprime.\n");
                        pw.flush();
                    }else{
                        pw.print("Suppression non reussi.\n");
                        pw.flush();
                    }
                
                }catch(Exception e){
                        pw.print("Erreur essayez SUPR -n <id annonce>\n");
                        pw.flush();
                }
                break;
            default:break;
        }
    }
    
    public void clientToClient(int idClientREQ){
        ServeurThread st = null;
        try{
            availableClient = false;
            pw.print("Vous etes connecte au client "+ idClientREQ+"\n");
            pw.flush();
            st = donnee.getServeurThread(idClientREQ);
            setConnexionCC(true);
            while(getConnexionCC()){
                String str;
                str = br.readLine();
            
                if(str.equals("QUIT")){
                    if(st.getTargetClient()==getSTId()){
                        st.sendToClient("Vous etes deconnecte");
                        st.setConnexionCC(false);
                    }
                    sendToClient("Vous etes deconnecte\n");
                    break;
                }
                if(getConnexionCC())
                st.sendToClient(str);
            }
        }catch(Exception e){
//              e.printStackTrace();
             st.sendToClient("client "+idClientREQ+" deconnecte tapez QUIT");
        }finally{
            setConnexionCC(false);
            this.targetClient = -1;
            availableClient = true;
        }
    }
    public void postRequest(String post){
        String annonce = post.substring(4);
        Message m = new Message(id,annonce);
        donnee.addMessage(m);
        pw.print("Annonce enregistre avec l'id "+m.getId()+"\n");
        pw.flush();
        
    }
    
    
    public void recqRequest(String str){
        try{
            int tc;
            ServeurThread st;
            String tab[] = str.split("\\s+");
            if (tab.length < 2){
                pw.print("Mauvaise commande connection non etablie.\n");
                pw.flush();
                return;
            }
            tc = Integer.parseInt(tab[1]);
            st = donnee.getServeurThread(tc);
            if(st.getTargetClient()!=getSTId()){
                pw.print("Le client "+st.getSTId()+" n'a pas effectue de demande de connexion\n");
                pw.flush();
                return;
            }
            if(tab[0].equals("NONE")){
                
                st.setResponse("NONE");
                st.setWaitingResponse(false);
                
                return;
            }
            
            st.setResponse("OKAY");
            st.setWaitingResponse(false);
            this.targetClient = tc;
            clientToClient(tc);
        }catch(Exception e){
//             e.printStackTrace();
            if(e instanceof NumberFormatException){
                pw.print("id invalide\n");
                pw.flush();
            }
        }
    }
    
   
    public void sendToClient(String msg){
        pw.print(msg+"\n");
        pw.flush();
   }

}