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
public class Box extends Prop{
    
    //Aparece en tiempo corto
    //Si el jugador colisiona con él, gana velocidad en X negativa durante 2 segundos
    
    public Box(float[] pos) {
        super(pos, new int[]{});
    }

    @Override
    public void onCollision(Racer raz) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
