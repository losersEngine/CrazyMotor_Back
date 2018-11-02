package losersengine.back.CrazyMotors_Back.Objects;

/**
 *
 * @author Brisin
 */
public class Fall extends Prop{
    
    //Aparece en tiempo largo
    //Si el jugador colisiona con él, gana velocidad en Y negativa hasta que llegue al piso de abajo
    
    public Fall(float[] pos) {
        super(pos, new int[]{});
    }

    @Override
    public void onCollision(Racer raz) {
        
        int[] vel = raz.getVel();
        raz.setStateAct(3);
        
        raz.setVel(new int[]{vel[0], -15});
        raz.setLineaActual(0);
    }
    
}
