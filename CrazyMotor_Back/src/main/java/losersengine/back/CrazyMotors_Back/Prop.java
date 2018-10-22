/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back;

/**
 *
 * @author brisin
 */
public abstract class Prop {
    
    protected float[] position = new float[2];
    
    public Prop(float[] pos){
        
        position[0] = pos[0];
        position[1] = pos[1];
        
    }
    
}
