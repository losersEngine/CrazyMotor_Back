package losersengine.back.CrazyMotors_Back.Objects;

/**
 *
 * @author Brisin
 */
public class Trampoline extends Prop{
    
    //Aparece en tiempo largo
    //Si el jugador colisiona con él, gana velocidad en Y hasta que llegue al piso de arriba
    
    public Trampoline(float[] pos) {
        super(pos, new int[]{20, 50});
    }

    @Override
    public void onCollision(Racer raz) {
        int[] vel = raz.getVel();
        raz.setStateAct(3);
        
        raz.setVel(new int[]{vel[0], 15});
        raz.setLineaActual(1);
    }
    
}
