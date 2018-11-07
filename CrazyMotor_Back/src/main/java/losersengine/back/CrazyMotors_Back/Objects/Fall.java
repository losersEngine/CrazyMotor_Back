package losersengine.back.CrazyMotors_Back.Objects;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Brisin
 */
public class Fall extends Prop{
    
    //Aparece en tiempo largo
    //Si el jugador colisiona con él, gana velocidad en Y negativa hasta que llegue al piso de abajo
    
    private ScheduledExecutorService scheduler;
    
    public Fall(float[] pos) {
        super(pos, new int[]{});
        
        this.type = "fall";
    }

    @Override
    public void onCollision(Racer raz) {
        
        int[] vel = raz.getVel();
        raz.setStateAct(3);
        
        raz.setVel(new int[]{vel[0], -15});
        raz.setLineaActual(0);
        
        this.state = 1;
        scheduler.schedule(() -> {this.toBreak = true;}, 1, TimeUnit.SECONDS);
    }
    
}
