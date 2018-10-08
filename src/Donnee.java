import java.util.*;
import java.lang.Thread;

public class Donnee{

    private List<Message> messages;
    private List<ServeurThread> clients;
    
    public Donnee(){
        messages = new ArrayList<Message>();
        clients = new ArrayList<ServeurThread>();
    }
    
    public synchronized List<Message> getMessages(){
        return messages;
    }
    
    public synchronized List<Message> getClientMessages(int idClient){
        List<Message> tmp = new ArrayList<Message>();
        for(Message m: messages){
            if(m.getIdClient()==idClient){
                tmp.add(m);
            }
        }
        return tmp;
    }
    
    public synchronized void addMessage(Message m){
        messages.add(m);
    }
    
     public synchronized Message getMessage(int id){
        Message mess = null;
        for(Message m: messages){
            if(m.getId()==id){
                mess = m;
            }
        }
        
        return mess;
    }
    
    public synchronized ServeurThread getAuthor(int id_message){
        int id_client = 0;
        for(Message m: messages){
            if(m.getId()==id_message){
                id_client = m.getIdClient();
                break;
            }
        }
        for(ServeurThread st : clients){
            if(st.getSTId() == id_client)
                return st;
        }
        
        return null;
        
    }
    
    public synchronized ServeurThread getServeurThread(int id){
        for(ServeurThread st : clients){
            if(st.getSTId() == id){
                return st;
            }
        }
        return null;
    }
    
    public synchronized void serveurNotAvailable(){
        for(ServeurThread st: clients){
            st.sendToClient("Serveur non disponible.");
            st.sendToClient("deconnexion");
        }
    }
    public synchronized void delClientMessage(int idClient){
        List<Message> tmp = new ArrayList<Message>();
        for(Message m: messages){
            if(m.getIdClient()==idClient){
                tmp.add(m);
            }
        }
        
        messages.removeAll(tmp);
    }
    
    public synchronized boolean delMessage(int id,int id_client){
        Message m = getMessage(id);
        if(m.getIdClient()==id_client)
            return messages.remove(m);
        return false;
    }
    
    public synchronized void addClient(ServeurThread t){
        clients.add(t);
    }
    
    public synchronized void delClient(ServeurThread t){
        clients.remove(t);
    }

}