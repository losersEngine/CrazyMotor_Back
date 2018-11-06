package losersengine.back.CrazyMotors_Back.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author Brisin
 */
public class Racer {
    
    private ScheduledExecutorService scheduler;
    
    public static List<String> states = new ArrayList<>();
    
    //1280, 720
    private final static int[] LINE_HEIGHTS = new int[]{150, 450};
    
    private String stateAct;
    
    private final int id;
    private final WebSocketSession session;
    private final String name;
    
    private int lineaActual;
    
    //Aceleración
    //Posición en X e Y
    private int pos[];
    private int vel[];
    
    //Collider (Ancho y alto en "radio")
    private final static int[] collider = new int[]{10, 80};
    
    private int nitroLvl;
    
    private boolean isNitroPressed;
    private boolean isJumpPressed;
    
    public Racer(int i, WebSocketSession s, String n){
        this.id = i;
        this.session = s;
        this.name = n;
        
        this.pos = new int[]{0,0};
        this.vel = new int[]{0,0};
        
        this.nitroLvl = 0;
        
        this.isJumpPressed = false;
        this.isNitroPressed = false;
        
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
    
    public void update(List<Prop> props){
        
        int index = states.indexOf(stateAct);
        
        switch(index){
        
            case 0: //Avanzando
                
                int i = 0;
                
                //Colisión
                
                while(i < props.size() && states.indexOf(stateAct) == 0){
                
                    Prop propAct = props.get(i);
                    if(propAct.isColliding(this)){
                        propAct.onCollision(this);
                    }
                    
                    i++;
                    
                }
                
                //COMPROBAR SI NITRO
                if(this.isNitroPressed){
                
                    this.vel[0] = 4;
                    
                }
                
                if(states.indexOf(stateAct) == 0){
                    //Actualizar posición
                    this.updatePosition();
                    //VelX-- hasta llegar a 0 (Por si el nitro le ha subido la velocidad)
                    vel[0]--;
                    if(vel[0] < 0)
                        vel[0] = 0;
                }
                
                //COMPROBAR SI SALTA
                if(this.isJumpPressed){
                
                    this.vel[1] = 7;
                    this.stateAct = states.get(2);
                    
                }

                break;
                
            case 1: //Golpeado
                
                //No colisión
                //Actualizar posición
                this.updatePosition();
                //VelY-- hasta llegar a la línea actual
                vel[1]--;
                if(pos[1] <= LINE_HEIGHTS[this.getLineaActual()]){
                    vel[1] = 0;
                    pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                }
                        
                
                break;
                
            case 2: //Saltando
                
                int j = 0;
                
                //Colisión
                
                while(j < props.size() && states.indexOf(stateAct) == 0){
                
                    Prop propAct = props.get(j);
                    if(propAct.isColliding(this)){
                        propAct.onCollision(this);
                    }
                    
                    j++;
                    
                }
                
                if(states.indexOf(stateAct) == 2){
                    //Actualizar posición
                    this.updatePosition();
                    //VelX-- hasta llegar a 0 (Por si el nitro le ha subido la velocidad)
                    vel[0]--;
                    if(vel[0] < 0)
                        vel[0] = 0;
                    
                    this.updatePosition();
                    //VelY-- hasta llegar a la línea actual
                    vel[1]--;
                    if(pos[1] <= LINE_HEIGHTS[this.getLineaActual()]){
                        vel[1] = 0;
                        pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                    }
                }
                
                break;
                
            case 3: //CambioLinea
                
                //No colisión
                //Actualizar posición
                this.updatePosition();

                if(this.vel[1] > 0){
                
                    if(pos[1] >= LINE_HEIGHTS[this.getLineaActual()]){
                        vel[1] = 0;
                        pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                        this.stateAct = states.get(0);
                    }
                    
                } else {
                    
                    if(pos[1] <= LINE_HEIGHTS[this.getLineaActual()]){
                        vel[1] = 0;
                        pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                        this.stateAct = states.get(0);
                    }
                
                }
                
                break;
                
            default:
                break;
        
        }
    }
    
    public void updatePosition(){
    
        this.pos[0] = this.pos[0] + this.vel[0];
        this.pos[1] = this.pos[1] + this.vel[1];
        
        if (pos[0] <= 15){
        
            pos[0] = 15;
            vel[0] = 0;
            
            int stateToChange = (this.vel[1] == 0) ? 0 : 2;
            this.stateAct = states.get(stateToChange);
            
        }
        
        if (pos[0] >= 720){
        
            pos[0] = 720;
            vel[0] = 0;
            
        }
    
    }
    
    public void stopGolpe(){
        scheduler.schedule(() -> {
            int vel[] = this.getVel();
        
            this.setVel(new int[]{0, vel[1]});
            int state = (vel[1]==0) ? 0 : 2; //Si está cayendo o subiendo, se mantiene el state en saltando, si no, en avanzando

            this.setStateAct(state);
        }, 2, TimeUnit.SECONDS);
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

    public int getLineaActual() {
        return lineaActual;
    }

    public void setLineaActual(int lineaActual) {
        this.lineaActual = lineaActual;
    }

}
