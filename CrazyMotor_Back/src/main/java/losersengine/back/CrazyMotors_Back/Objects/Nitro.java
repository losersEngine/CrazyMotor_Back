package losersengine.back.CrazyMotors_Back.Objects;

/**
 *
 * @author brisin
 */
public class Nitro extends Prop{
    
    //Aparece en tiempo corto
    //Si el jugador colisiona con él, gana carga de nitro que puede usar para ganar velocidad en X
    
    public Nitro(float[] pos) {
        super(pos, new int[]{});
    }

    @Override
    public void onCollision(Racer raz) {
        
        raz.setNitroLvl(raz.getNitroLvl() + 20);
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
