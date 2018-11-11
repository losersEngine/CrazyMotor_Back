package losersengine.back.CrazyMotors_Back;

import losersengine.back.CrazyMotors_Back.Objects.Racer;
import losersengine.back.CrazyMotors_Back.Objects.Prop;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
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
    
    //1280, 720
    public final static float[] DIMENSIONS = new float[]{1800.0f, 1080.0f};
    public final static float[] LINE_HEIGHTS = new float[]{950.0f, 370.0f};
    private final static long TIME_BETWEEN = 10;
    private final static long TIME_FINAL = 120;
    private float frame;
    
    private ConcurrentHashMap<Integer, Racer> racers; 
    private AtomicInteger numRacers; //Hacer que sólo se juegue cuando hayan 2 jugadores
    private List<Prop> props;
    
    //Línea abajo / línea arriba
    private List<PerlinNoise> noiseLines;
    private float[] noiseValues;

    private float difficulty = 1;
    private int id;
    
    private float velGame;
    private boolean inGame;

    private ScheduledExecutorService scheduler;
    private FinishLine finish;
    
    public RaceGame(int i, float dif){
        
        scheduler = Executors.newScheduledThreadPool(4);

        racers = new ConcurrentHashMap<>();
        numRacers = new AtomicInteger();
        inGame = false;
        difficulty = dif;
        id = i;
        props = new CopyOnWriteArrayList<>();
        
        noiseLines = new ArrayList<>();
        noiseLines.add(new PerlinNoise(rnd.nextInt()));
        noiseLines.add(new PerlinNoise(rnd.nextInt()));
        
        noiseValues = new float[]{50.0f, 50.0f};
        
        velGame = -3.0f * dif;
        frame = 0.0f;
        
        finish = null;

    }
    
    public void addRacer(Racer racer) {

        synchronized(racer){
            racers.put(racer.getId(), racer);
        }
        
        //////////////////////////////////////////////////////////////////////
        Collection<Racer> players = this.getRacers();
        StringBuilder playersInfo = new StringBuilder();
        playersInfo.append("{ \"function\": \"join\", \"params\": { \"pj\": [ ");

        Iterator<Racer> toSend = players.iterator();
        while(toSend.hasNext()){
            Racer act = toSend.next();
            
            playersInfo.append(String.format("{ \"id\": %d, \"pos\": [ %s, %s ] }", act.getId(), act.getPos()[0], act.getPos()[1]));

            if(toSend.hasNext())
                playersInfo.append(", ");
        }

        playersInfo.append(" ] } }");

        try {
            System.out.println(playersInfo.toString());
            this.broadcast(playersInfo.toString());
        } catch (Exception ex) {
            Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
        }

        int numberPlayers = numRacers.incrementAndGet();
        
        if (numberPlayers == 2){
            for (int i = 1; i < 4; i++){
                int toNumber = 4-i;
                scheduler.schedule(() -> countdown(toNumber), i, TimeUnit.SECONDS);
            }
            
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    try {
                        String msg = "{ \"function\": \"start\", \"params\": {} }";
                        broadcast(msg);
                        startTimer();
                    } catch (Exception ex) {
                        Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }, 4, TimeUnit.SECONDS);
        }

    }
    
    public void countdown(int i){
        try {
            String msg = "{ \"function\": \"countdown\", \"params\": { \"count\": " + i + "} }";
            this.broadcast(msg);
        } catch (Exception ex) {
            Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public Collection<Racer> getRacers() {
            return racers.values();
    }
    
    public void removePlayer(Racer racer) {

        synchronized(racer){
            racers.remove(racer.getId());
        }

        int count = numRacers.decrementAndGet();

        if (count == 1) {
            /*if (scheduler != null) {
                scheduler.shutdown();
            }*/
            
            Collection<Racer> players = this.getRacers();
            this.stopTimer((Racer)players.toArray()[0]);
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
    
    public void step(){

        List<Prop> toDestroy = new ArrayList<>();
        
        synchronized(props){
            for(Prop p : props){
                p.update(velGame);

                if(p.isToBreak())
                    toDestroy.add(p);
            }
        
            for(Racer r : this.getRacers()){
                r.update(props);
            }
        

            for(Prop d : toDestroy){
                System.out.println("Ey");
                props.remove(d);
                System.out.println("Ey 2");
            }
        }
        
        this.addProp();
        
        //////////////////////////////////////////////////////////////////////////
        StringBuilder playersInfo = new StringBuilder();
        
        Collection<Racer> players = this.getRacers();
        
        playersInfo.append("\"pj\": [ ");
        
        Iterator<Racer> toSendR = players.iterator();
        while(toSendR.hasNext()){
            Racer act = toSendR.next();
            
            playersInfo.append(String.format("{ \"id\": %d, \"state\": \"%s\", \"pos\": [ %s, %s ], \"nitroLvl\": %s }", act.getId(), act.getStateAct(), act.getPos()[0], act.getPos()[1], act.getNitroLvl()));
            
            if(toSendR.hasNext())
                playersInfo.append(", ");
        }
        playersInfo.append(" ]");
        
        //////////////////////////////////////////////////////////////////////////
        StringBuilder propsInfo = new StringBuilder();
        
        propsInfo.append("\"items\": [ ");
        
        Iterator<Prop> toSend = props.iterator();
        while(toSend.hasNext()){
            Prop act = toSend.next();
            
            propsInfo.append(String.format("{ \"type\": \"%s\", \"pos\": [ %s, %s ], \"state\": %d }", act.getType(), act.getPosition()[0], act.getPosition()[1], act.getState()));
            
            if(toSend.hasNext())
                propsInfo.append(", ");
        }
        
        propsInfo.append(" ]");
        
        //////////////////////////////////////////////////////////////////////////
        StringBuilder msg = new StringBuilder();
        
        msg.append(String.format("{ \"function\": \"update\", \"params\": { %s, %s } }", playersInfo.toString(), propsInfo.toString()));
        
        try {
            this.broadcast(msg.toString());
            //////////////////////////////////////////////////////////////////////////
            if(frame%100 == 0){
                float percentaje = frame / TIME_FINAL;
                
                if(percentaje > 100.0f)
                    percentaje = 100.0f;
                
                String percent = "{ \"function\": \"updateGoal\", \"params\": { \"percent\": " + percentaje + " } }";
                
                this.broadcast(percent);
            }
            //////////////////////////////////////////////////////////////////////////
        } catch (Exception ex) {
            Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        frame++;

    }
    
    public void addProp(){
        
        if (frame%3 == 0){
        
            int varianza;
            float cambioAbajo = noiseLines.get(0).getValue(frame * 1.05f);
            float cambioArriba = noiseLines.get(1).getValue(frame * 1.05f);

            float corte = Math.abs(cambioAbajo - cambioArriba);

            if (corte < 0.8f){
                
                int probArriba = rnd.nextInt(100);
                int probAbajo = rnd.nextInt(100);

                Prop pToAddArriba = null;
                Prop pToAddAbajo = null;

                varianza = rnd.nextInt(200) + 50;

                if (probAbajo < 55){
                    pToAddAbajo = new Box(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[0]});
                } else if (probAbajo < 80){
                    pToAddAbajo = new Nitro(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[0] - 100});
                } else if (probAbajo < 95){
                    pToAddAbajo = new Laser(new float[]{DIMENSIONS[0] + varianza, DIMENSIONS[1] - 110});
                    //Dar nitro a players

                    for(Racer r: this.getRacers()){
                        r.setNitroLvl(r.getNitroLvl() + 5);
                    }
                } else {
                    pToAddAbajo = new Trampoline(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[0] + 5});
                }

                varianza = rnd.nextInt(200) + 50;

                if (probArriba < 55){
                    pToAddArriba = new Box(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[1]});
                } else if (probArriba < 80){
                    pToAddArriba = new Nitro(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[1] - 100});
                } else if (probArriba < 95){
                    pToAddArriba = new Laser(new float[]{DIMENSIONS[0] + varianza, DIMENSIONS[1] -110});
                    //Dar nitro a players

                    for(Racer r: this.getRacers()){
                        r.setNitroLvl(r.getNitroLvl() + 5);
                    }
                } else {
                    pToAddArriba = new Fall(new float[]{DIMENSIONS[0] + varianza ,LINE_HEIGHTS[1] + 190});
                }
                
                synchronized(props){
                    props.add(pToAddArriba);
                    props.add(pToAddAbajo);
                }
                
            }
        }
        
    }
    
    public void startTimer() {
        
        RaceGame that = this;
        
        scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(() -> step(), TIME_BETWEEN, TIME_BETWEEN, TimeUnit.MILLISECONDS);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                finish = new FinishLine(new float[]{DIMENSIONS[0] + 100, 300}, that);
                synchronized(props){
                    props.add(finish);
                }
            }
        }, TIME_FINAL, TimeUnit.SECONDS);
        
    }
    
    public void stopTimer(Racer raz) {
            
        try {
            if (scheduler != null) {
                scheduler.shutdown();
            }

            //Avisa a los js que ha acabado la partida
            //TODO

            this.setInGame(false);
            
            String msg = "{ \"function\": \"finPartida\", \"params\": { \"winner\": " + raz.getId() + "} }";
            broadcast(msg);

        } catch (Exception ex) {
            Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }
    
    ///////////////////////////////////////////////////////////////////////////////////////////////////
        
    public synchronized int getNum(){
        return numRacers.get();
    }

    public float getDifficulty() {
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

    public int getId() {
        return id;
    }
    
}
