/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back.Objects;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author Brisin
 */
public class Racer {
    
    public static List<String> states = new ArrayList<>();
    //{"Avanzando", "Golpeado", "Saltando", "CambioLinea"};
    
    private String stateAct;
    
    private final int id;
    private final WebSocketSession session;
    private final String name;
    
    //Aceleración
    //Posición en X e Y
    private int pos[];
    private int vel[];
    
    //Collider (Ancho y alto en "radio")
    private final static int[] collider = new int[]{10, 80};
    
    private int nitroLvl;
    
    public Racer(int i, WebSocketSession s, String n){
        this.id = i;
        this.session = s;
        this.name = n;
        
        this.pos = new int[]{0,0};
        this.vel = new int[]{0,0};
        
        this.nitroLvl = 0;
        
        states.add("Avanzando");
        states.add("Golpeado");
        states.add("Saltando");
        states.add("CambioLinea");
        
        this.stateAct = states.get(0);
    }
    
    public synchronized void sendMessage(String msg) throws Exception {
        if(this.session.isOpen())
            this.session.sendMessage(new TextMessage(msg));
    }
    
    public void update(Collection<Prop> props){
        
        int index = states.indexOf(stateAct);
        
        switch(index){
        
            case 0:
                break;
                
            case 1:
                break;
                
            case 2:
                break;
                
            case 3:
                break;
                
            default:
                break;
        
        }
    }

    public WebSocketSession getSession() {
        return session;
    }

    public String getName() {
        return name;
    }
    
    public int getId(){
        return id;
    }

    public int[] getPos() {
        return pos;
    }

    public void setPos(int[] pos) {
        this.pos = pos;
    }

    public int[] getVel() {
        return vel;
    }

    public void setVel(int[] vel) {
        this.vel = vel;
    }

    public int[] getCollider() {
        return collider;
    }
    
    public int getNitroLvl() {
        return nitroLvl;
    }

    public void setNitroLvl(int lvl) {
        this.nitroLvl = lvl;
        
        if(this.nitroLvl < 0)
            this.nitroLvl = 0;
        
        if(this.nitroLvl > 100)
            this.nitroLvl = 100;
    }

    public String getStateAct() {
        return stateAct;
    }

    public void setStateAct(int st) {
        this.stateAct = states.get(st);
    }
    
}
