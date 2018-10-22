/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back;

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
    
    public Racer(int i, WebSocketSession s, String n){
        this.id = i;
        this.session = s;
        this.name = n;
    }
    
    protected synchronized void sendMessage(String msg) throws Exception {
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
    
}
