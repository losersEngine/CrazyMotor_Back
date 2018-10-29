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
public class Laser extends Prop{
    
    //Aparece en tiempo medio
    //Si el jugador colisiona con él, gana velocidad en X negativa hasta llegar al inicio del mapa
    //Cuando aparece se les da un 5% de nitro a todos los jugadores
    
    public Laser(float[] pos) {
        super(pos, new int[]{});
    }

    @Override
    public void onCollision(Racer raz) {
        raz.setStateAct(1);
        
        raz.setNitroLvl(0);
        raz.setVel(new int[]{-5, 0});
        
        //throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
}
