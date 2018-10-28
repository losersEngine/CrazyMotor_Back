/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back.Objects;

import java.util.Collection;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author Brisin
 */
public class Racer {
    
    private final int id;
    private final WebSocketSession session;
    private final String name;
    
    //Aceleración
    //Posición en X e Y
    private int pos[];
    private float vel[];
    
    //Collider (Ancho y alto en "radio")
    private final static int[] collider = new int[]{3, 5};
    
    public Racer(int i, WebSocketSession s, String n){
        this.id = i;
        this.session = s;
        this.name = n;
        
        this.pos = new int[]{0,0};
        this.vel = new float[]{0.0f,0.0f};
    }
    
    public synchronized void sendMessage(String msg) throws Exception {
        if(this.session.isOpen())
            this.session.sendMessage(new TextMessage(msg));
    }
    
    public void update(Collection<Prop> props){
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

    public float[] getVel() {
        return vel;
    }

    public void setVel(float[] vel) {
        this.vel = vel;
    }

    public int[] getCollider() {
        return collider;
    }
    
}
