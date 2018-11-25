package losersengine.back.CrazyMotors_Back.Objects;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 *
 * @author Brisin
 */
public class Racer {
    private ScheduledExecutorService scheduler;
    
    public static List<String> states = new ArrayList<>();
    
    private Lock cambioState = new ReentrantLock();
    
    //1280, 720
    public final static float[] DIMENSIONS = losersengine.back.CrazyMotors_Back.RaceGame.DIMENSIONS;
    public final static float[] LINE_HEIGHTS = losersengine.back.CrazyMotors_Back.RaceGame.LINE_HEIGHTS;
    
    public final static float GRAVITY = 0.3f;//0.98f;
    
    private String stateAct;
    
    private final int id;
    private final WebSocketSession session;
    
    private int lineaActual;
    
    //Aceleración
    private float jumpAc;
    //Posición en X e Y
    private float pos[];
    private float vel[];
    
    //Collider (Ancho y alto en "radio")
    private final static int[] collider = new int[]{27, -180};
    
    private float nitroLvl;
    
    private boolean isNitroPressed;
    private boolean isJumpPressed;
    
    public Racer(int i, float[] p, WebSocketSession s){
        this.id = i;
        this.session = s;
        
        this.pos = p;
        this.vel = new float[]{0,0};
        this.jumpAc = 0.0f;
        
        this.lineaActual = (this.pos[1] == LINE_HEIGHTS[0]) ? 0:1;
        
        this.nitroLvl = 10.0f;
        
        this.isJumpPressed = false;
        this.isNitroPressed = false;
        
        scheduler = Executors.newScheduledThreadPool(1);
        
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
                
                cambioState.lock();
                try{
                    if(states.indexOf(stateAct) == 0){
                        //Actualizar posición
                        this.updatePosition();

                        this.vel[0] -= 0.1f;
                        if(this.vel[0] < 0)
                            this.vel[0] = 0;
                        
                        //COMPROBAR SI NITRO
                        if(this.isNitroPressed && this.nitroLvl > 0){
                            this.vel[0] = 3.0f;
                            this.nitroLvl -= 0.1f;
                        }
                        
                        //COMPROBAR SI SALTA
                        if(this.isJumpPressed){
                            this.vel[1] = -9.3f;
                            this.stateAct = states.get(2);
                        }
                    }
                } finally{
                    cambioState.unlock();
                }

                break;
                
            case 1: //Golpeado
                
                //No colisión
                //Actualizar posición
                this.updatePosition();

                this.vel[1]+= GRAVITY;
                if(this.pos[1] <= LINE_HEIGHTS[this.getLineaActual()]){
                    this.vel[1] = 0.0f;
                    this.pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                }
                
                int k = 0;
                
                //Colisión
                
                while(k < props.size() && states.indexOf(stateAct) == 1){
                
                    Prop propAct = props.get(k);
                    if(propAct.getType().equals("finishLine") && propAct.isColliding(this)){
                        propAct.onCollision(this);
                    }
                    
                    k++;
                    
                }
                        
                
                break;
                
            case 2: //Saltando
                
                int j = 0;
                
                //Colisión
                
                while(j < props.size() && states.indexOf(this.stateAct) == 2){
                
                    Prop propAct = props.get(j);
                    if(propAct.isColliding(this)){
                        propAct.onCollision(this);
                    }
                    
                    j++;
                    
                }
                
                cambioState.lock();
                try{
                    if(states.indexOf(this.stateAct) == 2){
                        //Actualizar posición
                        this.updatePosition();

                        this.vel[1] += ((this.isJumpPressed) ? GRAVITY/2 : GRAVITY);

                        if(this.pos[1] >= LINE_HEIGHTS[this.getLineaActual()]){
                            this.vel[1] = 0.0f;
                            this.pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                            this.stateAct = states.get(0);
                        }
                    }
                } finally {
                    cambioState.unlock();
                }
                
                break;
                
            case 3: //CambioLinea
                
                //No colisión
                //Actualizar posición
                this.updatePosition();

                cambioState.lock();
                try{
                    if(this.vel[1] < 0){

                        if(pos[1] <= LINE_HEIGHTS[this.getLineaActual()]){
                            vel[1] = 0;
                            pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                            this.stateAct = states.get(0);
                        }

                    } else {

                        if(pos[1] >= LINE_HEIGHTS[this.getLineaActual()]){
                            vel[1] = 0;
                            pos[1] = LINE_HEIGHTS[this.getLineaActual()];
                            this.stateAct = states.get(0);
                        }

                    }
                } finally {
                    cambioState.unlock();
                }
                
                int u = 0;
                
                //Colisión
                
                while(u < props.size() && states.indexOf(stateAct) == 3){
                
                    Prop propAct = props.get(u);
                    if(propAct.getType().equals("finishLine") && propAct.isColliding(this)){
                        propAct.onCollision(this);
                    }
                    
                    u++;
                    
                }
                
                break;
                
            default:
                break;
        
        }
    }
    
    public void updatePosition(){
    
        this.pos[0] = this.pos[0] + this.vel[0];
        this.pos[1] = this.pos[1] + this.vel[1];
        
        if (pos[0] <= 60){
        
            pos[0] = 60;
            vel[0] = 0;
            
            int stateToChange = (this.vel[1] == 0) ? 0 : 2;
            this.stateAct = states.get(stateToChange);
            
        }
        
        if (pos[0] >= DIMENSIONS[0] - 60){
        
            pos[0] = DIMENSIONS[0] - 60;
            vel[0] = 0;
            
        }
    
    }
    
    public void stopGolpe(){
        scheduler.schedule(() -> {
            float vel[] = this.getVel();
        
            this.setVel(new float[]{0, vel[1]});
            int state = (vel[1]==0) ? 0 : 2; //Si está cayendo o subiendo, se mantiene el state en saltando, si no, en avanzando

            cambioState.lock();
            try {
                this.setStateAct(state);
            } finally {
                cambioState.unlock();
            }
        }, 2, TimeUnit.SECONDS);
    }

    public WebSocketSession getSession() {
        return session;
    }
    
    public int getId(){
        return id;
    }

    public float[] getPos() {
        return pos;
    }

    public void setPos(float[] pos) {
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
    
    public float getNitroLvl() {
        return nitroLvl;
    }

    public void setNitroLvl(float lvl) {
        this.nitroLvl = lvl;
        
        if(this.nitroLvl < 0.0f)
            this.nitroLvl = 0.0f;
        
        if(this.nitroLvl > 100.0f)
            this.nitroLvl = 100.0f;
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

    public synchronized boolean isIsNitroPressed() {
        return isNitroPressed;
    }

    public synchronized void setIsNitroPressed(boolean isNitroPressed) {
            this.isNitroPressed = isNitroPressed;
    }

    public synchronized boolean isIsJumpPressed() {
        return isJumpPressed;
    }

    public synchronized void setIsJumpPressed(boolean isJumpPressed) {
            this.isJumpPressed = isJumpPressed;
    }
    
    public static int getColX(){
        return collider[0];
    }
    
    public void cambioLinea(float velY, int newLine){
        cambioState.lock();
        try{
            this.setStateAct(3);
        } finally {
            cambioState.unlock();
        }
        
        this.setVel(new float[]{vel[0], velY});
        this.setLineaActual(newLine);
    }

}
