package losersengine.back.CrazyMotors_Back;

import losersengine.back.CrazyMotors_Back.Objects.Racer;
import losersengine.back.CrazyMotors_Back.Objects.Prop;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import losersengine.back.CrazyMotors_Back.Objects.FinishLine;

/**
 *
 * @author Brisin
 */
public class RaceGame {
    
    public Random rnd = new Random(System.currentTimeMillis());
    private ObjectMapper mapper = new ObjectMapper();
    
    private final static long TIME_BETWEEN = 100;
    
    private ConcurrentHashMap<Integer, Racer> racers; 
    private AtomicInteger numRacers; //Hacer que sólo se juegue cuando hayan 2 jugadores
    private List<Prop> props;

    private long difficulty = 1;
    private int velGame;
    private boolean inGame;

    private ScheduledExecutorService scheduler;
    private FinishLine finish;
    
    public RaceGame(long dif, String c){

        racers = new ConcurrentHashMap<>();
        numRacers = new AtomicInteger();
        inGame = false;
        difficulty = dif;
        props = new CopyOnWriteArrayList<>();
        velGame = -5;
        
        finish = null;

    }
    
    public void addRacer(Racer racer) {

        synchronized(racer){
            racers.put(racer.getId(), racer);
        }

        numRacers.getAndIncrement();

    }
    
    public Collection<Racer> getRacers() {
            return racers.values();
    }
    
    public void removePlayer(Racer racer) {

        synchronized(racer){
            racers.remove(Integer.valueOf(racer.getId()));
        }

        int count = numRacers.decrementAndGet();


        if (count == 0) {
            if (scheduler != null) {
                scheduler.shutdown();
            }
        }
        
    }
    
    public synchronized void broadcast(String message) throws Exception {

        for (Racer rac : getRacers()) {
            try {

                System.out.println("Sending message " + message + " to " + rac.getId());
                rac.sendMessage(message);

            } catch (Throwable ex) {
                System.err.println("Execption sending message to snake " + rac.getId());
                ex.printStackTrace(System.err);
                removePlayer(rac);
            }
        }
        
    }
    
    public void step(){//TODO
        //Tener 3 variables de isPressed en racer, una por botón existente  -->Estos modifican su valor a partir de websockets desde front
        //Tener 3 estados en racer, corriendo-saltando-colisión             -->Estos van por Backend
        //Actualizar todos los racers
            //Victoria
            //Colisiones
            //Avance
            
        //Update props
        //Update racers
        
        for(Prop p : props){
            p.update(velGame);
        }
        
        for(Racer r : this.getRacers()){
            r.update(props);
            
            if(finish != null){
                finish.isColliding(r);
            }
        }
            
    }
    
    public synchronized void addProp(Prop p){
        props.add(p);
    }
    
    public void startTimer() {
        
        RaceGame that = this;
        
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> step(), TIME_BETWEEN/difficulty, TIME_BETWEEN/difficulty, TimeUnit.MILLISECONDS);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                finish = new FinishLine(new float[]{1080, 720}, new int[]{10, 720}, that);
            }
        }, 120, TimeUnit.SECONDS);
        
    }
    
    public void stopTimer(Racer raz) {
            
            try {
                if (scheduler != null) {
                    scheduler.shutdown();
                }
                
                //Avisa a los js que ha acabado la partida
                //TODO
                
                this.setInGame(false);
                
                ObjectNode n = mapper.createObjectNode();
                n.put("type","finPartida");
                //TODO
                //n.put("ganador", aux[0]);
                //n.put("puntos", aux[1]);
                broadcast(n.toString());
                
            } catch (Exception ex) {
                Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
	}
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
        
    public synchronized int getNum(){
        return numRacers.get();
    }

    public long getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(long difficulty) {
        this.difficulty = difficulty;
    }

    public boolean isInGame() {
        return inGame;
    }

    public void setInGame(boolean inGame) {
        this.inGame = inGame;
    }
    
}
