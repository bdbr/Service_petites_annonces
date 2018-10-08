public class Message{
    private static int id_compteur = 0;
    private int id;
    private String corps;
    private int idClient;
    
    
    public Message(int i,String c){
        id = id_compteur++;
        corps = c;
        idClient = i;
    
    }
    
    public int getIdClient(){
        return idClient;
    }
    public int getId(){
        return id;
    }
    public String getCorps(){
        return corps;
    }
    
    
}