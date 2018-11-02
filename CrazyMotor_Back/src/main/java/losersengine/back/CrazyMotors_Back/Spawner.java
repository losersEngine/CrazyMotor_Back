/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import losersengine.back.CrazyMotors_Back.Objects.Prop;

/**
 *
 * @author brisin
 */
public class Spawner{
    
    private float MAX_GAP, MIN_GAP;
    private Class objectToGenerate;
    
    private Random rnd;
    
    private RaceGame race;
    
    Thread t;
    
    public Spawner(float max, float min, Class o, RaceGame r){
    
        MAX_GAP = max;
        MIN_GAP = min;
        objectToGenerate = o;
        race = r;
        
        rnd = new Random(System.currentTimeMillis());
        
        t = new Thread(Generate());
    
    }
    
    public Runnable Generate(){
        return () -> {
            try {
                float sleepTime = rnd.nextFloat()*(MAX_GAP-MIN_GAP+1)+MIN_GAP;
                Thread.sleep((long) sleepTime);
                
                if(!Thread.interrupted()){
                    //race.addProp(new Prop());
                }
            } catch (InterruptedException ex) {
                Logger.getLogger(Spawner.class.getName()).log(Level.SEVERE, null, ex);
            }

        };
    }
    
}
