package losersengine.back.CrazyMotors_Back.Objects;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author brisin
 */
public class Laser extends Prop{
    
    //Aparece en tiempo medio
    //Si el jugador colisiona con él, gana velocidad en X negativa hasta llegar al inicio del mapa
    //Cuando aparece se les da un 5% de nitro a todos los jugadores
    
    private ScheduledExecutorService scheduler;
    
    public Laser(float[] pos) {
        super(pos, new int[]{45, -1300});
        
        scheduler = Executors.newScheduledThreadPool(1);
        
        this.type = "laser";
        
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                state = (state + 1) % 3;
            }
        }, 0, 2, TimeUnit.SECONDS);
    }

    @Override
    public void onCollision(Racer raz) {
        raz.setStateAct(1);
        
        raz.setNitroLvl(0);
        raz.setVel(new float[]{-5.0f, 0});

    }
    
}
