package losersengine.back.CrazyMotors_Back.Objects;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author Brisin
 */
public class Trampoline extends Prop{
    
    //Aparece en tiempo largo
    //Si el jugador colisiona con él, gana velocidad en Y hasta que llegue al piso de arriba
    
    private ScheduledExecutorService scheduler;
    
    public Trampoline(float[] pos) {
        super(pos, new int[]{20, 50});
    }

    @Override
    public void onCollision(Racer raz) {
        int[] vel = raz.getVel();
        raz.setStateAct(3);
        
        raz.setVel(new int[]{vel[0], 15});
        raz.setLineaActual(1);
        
        this.state = 1;
        scheduler.schedule(() -> {this.toBreak = true;}, 1, TimeUnit.SECONDS);
    }
    
}
