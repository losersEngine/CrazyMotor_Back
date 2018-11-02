package losersengine.back.CrazyMotors_Back.Objects;

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
        super(pos, new int[]{});
    }

    @Override
    public void onCollision(Racer raz) {
        
        raz.setStateAct(1);
        
        raz.setNitroLvl(0);
        raz.setVel(new int[]{-2, 0});
        
        scheduler.schedule(() -> stopGolpe(raz), 2, TimeUnit.SECONDS);
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public void stopGolpe(Racer raz){
        int vel[] = raz.getVel();
        
        raz.setVel(new int[]{0, vel[1]});
        int state = (vel[1]==0) ? 0 : 2; //Si está cayendo o subiendo, se mantiene el state en saltando, si no, en avanzando
        
        raz.setStateAct(state);
    }
    
}
