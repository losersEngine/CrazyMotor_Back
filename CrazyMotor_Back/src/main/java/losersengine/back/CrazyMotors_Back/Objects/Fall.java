package losersengine.back.CrazyMotors_Back.Objects;

import java.util.concurrent.Executors;
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
        super(pos, new int[]{100,-210});
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        this.type = "fall";
    }

    @Override
    public void onCollision(Racer raz) {
        if (raz.getLineaActual() == 1){
            this.state = 1;
            scheduler.schedule(() -> {this.toBreak = true;}, 1, TimeUnit.SECONDS);

            raz.cambioLinea(10.0f, 0);
        }
    }
    
}
