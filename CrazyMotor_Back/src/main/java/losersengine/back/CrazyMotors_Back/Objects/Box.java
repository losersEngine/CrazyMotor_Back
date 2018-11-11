package losersengine.back.CrazyMotors_Back.Objects;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author brisin
 */
public class Box extends Prop{
    
    //Aparece en tiempo corto
    //Si el jugador colisiona con él, gana velocidad en X negativa durante 2 segundos
    
    private ScheduledExecutorService scheduler;
    
    public Box(float[] pos) {
        super(pos, new int[]{50,-100});
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        this.type = "box";
    }

    @Override
    public void onCollision(Racer raz) {
        
        raz.setStateAct(1);
        
        raz.setNitroLvl(0);
        raz.setVel(new float[]{-2.0f, 0});
        
        this.state = 1;
        scheduler.schedule(() -> {this.toBreak = true;}, 1, TimeUnit.SECONDS);
        
        raz.stopGolpe();
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
