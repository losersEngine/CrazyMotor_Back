/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
    public void onCollision() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
