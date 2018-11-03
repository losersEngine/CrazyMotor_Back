package losersengine.back.CrazyMotors_Back;

import losersengine.back.CrazyMotors_Back.Objects.Racer;
import losersengine.back.CrazyMotors_Back.Objects.Prop;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.ArrayList;
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
import losersengine.back.CrazyMotors_Back.Objects.Box;
import losersengine.back.CrazyMotors_Back.Objects.Fall;
import losersengine.back.CrazyMotors_Back.Objects.FinishLine;
import losersengine.back.CrazyMotors_Back.Objects.Laser;
import losersengine.back.CrazyMotors_Back.Objects.Nitro;
import losersengine.back.CrazyMotors_Back.Objects.Trampoline;

/**
 *
 * @author Brisin
 */
public class RaceGame {
    
    public Random rnd = new Random(System.currentTimeMillis());
    private ObjectMapper mapper = new ObjectMapper();
    
    //1280, 720
    private final static int[] LINE_HEIGHTS = new int[]{150, 450};
    private final static long TIME_BETWEEN = 100;
    private int frame;
    
    private ConcurrentHashMap<Integer, Racer> racers; 
    private AtomicInteger numRacers; //Hacer que sólo se juegue cuando hayan 2 jugadores
    private List<Prop> props;
    
    //Línea abajo / línea arriba
    private List<PerlinNoise> noiseLines;
    private float[] noiseValues;

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
        
        noiseLines = new ArrayList<>();
        noiseLines.add(new PerlinNoise());
        noiseLines.add(new PerlinNoise());
        
        noiseValues = new float[]{0.5f, 0.5f};
        
        velGame = -5;
        frame = 1;
        
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
        
        this.addProp();
            
    }
    
    public void addProp(){
        
        //Comparamos con los valores anteriores
        float cambioAbajo = noiseLines.get(0).getValue(frame * (TIME_BETWEEN / 1000));
        float cambioArriba = noiseLines.get(1).getValue(frame * (TIME_BETWEEN / 1000));
        
        ///////////////////////////////////////////7
        if(cambioAbajo > 25 && cambioAbajo < 60){
            //Spawn Caja o láser, hacer un rnd
            int prob = rnd.nextInt(10);
            
            if (prob < 2){
                //Láser
                props.add(new Laser(new float[]{1080 , 0}));
            } else{
                //Caja
                props.add(new Box(new float[]{1080 ,LINE_HEIGHTS[0]}));
            }
            
        } else if(cambioAbajo > 60){
            //Spawn Trampolín
            props.add(new Trampoline(new float[]{1080 ,LINE_HEIGHTS[0]}));
        } else if(cambioAbajo < -35){
            //Spawn nitro
            props.add(new Nitro(new float[]{1080 ,LINE_HEIGHTS[0] + 30}));
        }
        
        ///////////////////////////////////////////7
        if(cambioArriba > 25 && cambioArriba < 60){
            //Spawn Caja o láser, hacer un rnd
            int prob = rnd.nextInt(10);
            
            if (prob < 2){
                //Láser
                props.add(new Laser(new float[]{1080 , 0}));
            } else{
                //Caja
                props.add(new Box(new float[]{1080 ,LINE_HEIGHTS[1]}));
            }
        } else if(cambioArriba > 60){
            //Spawn Fall
            props.add(new Fall(new float[]{1080 ,LINE_HEIGHTS[1]}));
        } else if(cambioArriba < -35){
            //Spawn nitro
            props.add(new Nitro(new float[]{1080 ,LINE_HEIGHTS[1] + 30}));
        }
        
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
