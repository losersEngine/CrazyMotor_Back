package losersengine.back.CrazyMotors_Back.Objects;

import java.util.concurrent.Executors;
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
        super(pos, new int[]{100, -30});
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        this.type = "trampoline";
    }

    @Override
    public void onCollision(Racer raz) {
        this.state = 1;
        scheduler.schedule(() -> {this.toBreak = true;}, 1, TimeUnit.SECONDS);
        
        raz.cambioLinea(-10.0f, 1); //1
    }
    
}
