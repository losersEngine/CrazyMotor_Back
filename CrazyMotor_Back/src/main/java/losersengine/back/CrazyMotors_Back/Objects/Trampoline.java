/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back.Objects;

/**
 *
 * @author Brisin
 */
public class Trampoline extends Prop{
    
    //Aparece en tiempo largo
    //Si el jugador colisiona con él, gana velocidad en Y hasta que llegue al piso de arriba
    
    public Trampoline(float[] pos) {
        super(pos, new int[]{});
    }

    @Override
    public void onCollision() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
