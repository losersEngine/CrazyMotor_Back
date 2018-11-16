package losersengine.back.CrazyMotors_Back;

import losersengine.back.CrazyMotors_Back.Objects.Racer;
import losersengine.back.CrazyMotors_Back.Objects.Prop;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
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
    private final static long TIME_BETWEEN = 5;
    private final static long TIME_FINAL = 120;
    private float frame;
    
    private Lock finalJuego = new ReentrantLock();
    private Lock listaObj = new ReentrantLock();
    private boolean endGame;
    
    private ConcurrentHashMap<Integer, Racer> racers; 
    private AtomicInteger numRacers; //Hacer que sólo se juegue cuando hayan 2 jugadores
    private List<Prop> propsAbajo;
    private List<Prop> propsArriba;
    
    //Línea abajo / línea arriba
    private List<PerlinNoise> noiseLines;
    private float[] noiseValues;

    private float difficulty = 1;
    private int id;
    
    private float velGame;

    private ScheduledExecutorService scheduler;
    private FinishLine finish;
    
    public RaceGame(int i, float dif){
        
        scheduler = Executors.newScheduledThreadPool(6);
        
        finalJuego.lock();
        try {
            endGame = false;
        } finally {
            finalJuego.unlock();
        }
        
        racers = new ConcurrentHashMap<>();
        numRacers = new AtomicInteger();
        difficulty = dif;
        id = i;
        propsAbajo = new ArrayList<>();
        propsArriba = new ArrayList<>();
        
        noiseLines = new ArrayList<>();
        noiseLines.add(new PerlinNoise(rnd.nextInt()));
        noiseLines.add(new PerlinNoise(rnd.nextInt()));
        
        noiseValues = new float[]{50.0f, 50.0f};
        
        velGame = -2.5f * dif;
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
        
        if (numberPlayers == 2 && !scheduler.isShutdown()){
            for (int i = 0; i < 3; i++){
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
    
    private void countdown(int i){
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

        int count = numRacers.get();

        if (count == 2) {
            Collection<Racer> players = this.getRacers();
            this.stopTimer((Racer)players.toArray()[0]);
            numRacers.decrementAndGet();
        }
        
    }
    
    public synchronized void broadcast(String message) throws Exception {

        for (Racer rac : getRacers()) {
            try {

                System.out.println("Sending message " + message + " to " + rac.getId());
                rac.sendMessage(message);

            } catch (Throwable ex) {
                System.err.println("Execption sending message to racer " + rac.getId());
                ex.printStackTrace(System.err);
                removePlayer(rac);
            }
        }
        
    }
    
    public void step(){

        List<Prop> toDestroyAbajo = new ArrayList<>();
        List<Prop> toDestroyArriba = new ArrayList<>();
        List<Prop> allProp = new ArrayList<>();
        
        listaObj.lock();
        try{
            for(Prop p : propsAbajo){
                p.update(velGame);

                if(p.isToBreak()){
                    toDestroyAbajo.add(p);
                } else {
                    allProp.add(p);
                }
            }
            
            for(Prop p : propsArriba){
                p.update(velGame);

                if(p.isToBreak()){
                    toDestroyArriba.add(p);
                } else {
                    allProp.add(p);
                }
            }
        
            for(Racer r : this.getRacers()){
                r.update(allProp);
            }
        
            for(Prop d : toDestroyAbajo){
                propsAbajo.remove(d);
            }
            
            for(Prop d : toDestroyArriba){
                propsArriba.remove(d);
            }
            
            this.addProp();
            
        } finally{
            listaObj.unlock();
        }
        
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
        
        Iterator<Prop> toSend = allProp.iterator();
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
        
        if (frame%200 == 0){
            
            float cambioAbajo = noiseLines.get(0).getValue(frame);
            float cambioArriba = noiseLines.get(1).getValue(frame);
            
            int varianza;
            
            Prop pToAddArriba = null;
            Prop pToAddAbajo = null;
            
            //////////////////////////////////////////////////////////////////////////
            
            Box aux;
            int index;
            Box existente;
            
            varianza = rnd.nextInt(50) + 100;
            
            if (cambioAbajo <= 10){ //Trampoline
                pToAddAbajo = new Trampoline(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[0] + 5});
            } else if (cambioAbajo >= 40 && cambioAbajo < 80){ //Caja simple o doble
                
                System.out.println("Caja Abajo");
                
                aux = new Box(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[0]});
                index = propsAbajo.size() - 1;
                existente = null;
                
                while ((index > -1) && (existente == null)){
                    if(propsAbajo.get(index).getType().equals("box"))
                        existente = (Box) propsAbajo.get(index);
                    
                    index--;
                }
                
                if (existente == null){
                    pToAddAbajo = aux;
                } else {
                    pToAddAbajo = (existente.isRanged(aux, frame)) ? null:aux;
                }

                if(cambioAbajo >= 70 && pToAddAbajo != null){ //Caja doble
                    Prop secondBox = new Box(new float[]{pToAddAbajo.getPosition()[0] + pToAddAbajo.getWidth(),LINE_HEIGHTS[0]});
                    propsAbajo.add(pToAddAbajo);
                    pToAddAbajo = secondBox;
                }
            
            } else if (cambioAbajo >= 80 && cambioAbajo <= 90){  //Nitro
                pToAddAbajo = new Nitro(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[0] - 100});
            } else if (cambioAbajo > 90){  //Laser
                pToAddAbajo = new Laser(new float[]{DIMENSIONS[0] + varianza, DIMENSIONS[1] - 110});
                //Dar nitro a players

                for(Racer r: this.getRacers()){
                    r.setNitroLvl(r.getNitroLvl() + 10);
                }
            }
            
            if(pToAddAbajo != null)
                propsAbajo.add(pToAddAbajo);
            
            //////////////////////////////////////////////////////////////////////////
            
            varianza = rnd.nextInt(50);
            
            if (cambioArriba <= 10){ //Fall
                pToAddArriba = new Fall(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[1] + 190});
            } else if (cambioArriba >= 40 && cambioArriba < 80){ //Caja simple
                
                aux = new Box(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[1]});
                index = propsArriba.size() - 1;
                existente = null;
                
                while ((index > -1) && (existente == null)){
                    if(propsArriba.get(index).getType().equals("box"))
                        existente = (Box) propsArriba.get(index);
                    
                    index--;
                }
                
                if (existente == null){
                    pToAddArriba = aux;
                } else {
                    pToAddArriba = (existente.isRanged(aux, frame)) ? null:aux;
                }
                
                if(cambioArriba >= 70 && pToAddArriba != null){ //Caja doble
                    Prop secondBox = new Box(new float[]{pToAddArriba.getPosition()[0] + pToAddArriba.getWidth(),LINE_HEIGHTS[1]});
                    propsArriba.add(pToAddArriba);
                    pToAddArriba = secondBox;
                }
            
            } else if (cambioArriba >= 80 && cambioArriba <= 90){  //Nitro
                pToAddArriba = new Nitro(new float[]{DIMENSIONS[0] + varianza,LINE_HEIGHTS[1] - 100});
            } else if (cambioArriba > 90){  //Laser
                pToAddArriba = new Laser(new float[]{DIMENSIONS[0] + varianza, DIMENSIONS[1] -110});
                //Dar nitro a players

                for(Racer r: this.getRacers()){
                    r.setNitroLvl(r.getNitroLvl() + 10);
                }
            }
            
            if(pToAddArriba != null)
                propsArriba.add(pToAddArriba);
            
        }
        
    }
    
    public void startTimer() {
        
        RaceGame that = this;
        
        scheduler.scheduleAtFixedRate(() -> step(), TIME_BETWEEN, TIME_BETWEEN, TimeUnit.MILLISECONDS);
        scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                listaObj.lock();
                try{
                    finish = new FinishLine(new float[]{DIMENSIONS[0] + 100, LINE_HEIGHTS[0]}, that);
                    propsAbajo.add(finish);
                    finish = new FinishLine(new float[]{DIMENSIONS[0] + 100, LINE_HEIGHTS[1]}, that);
                    propsArriba.add(finish);
                } finally{
                    listaObj.unlock();
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


            finalJuego.lock();
            try {
                if (!endGame){
                    String msg = "{ \"function\": \"finPartida\", \"params\": { \"winner\": " + raz.getId() + "} }";
                    broadcast(msg);
                    
                    endGame = true;
                }
            } finally {
                finalJuego.unlock();
            }
            

        } catch (Exception ex) {
            Logger.getLogger(RaceGame.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized int getNum() {
        return numRacers.get();
    }

    public float getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(long difficulty) {
        this.difficulty = difficulty;
    }

    public int getId() {
        return id;
    }

    public boolean isEndGame() {
        boolean game;
        
        finalJuego.lock();
        try {
            game = endGame;
        } finally {
            finalJuego.unlock();
        }
        
        return game;
    }
    
}
