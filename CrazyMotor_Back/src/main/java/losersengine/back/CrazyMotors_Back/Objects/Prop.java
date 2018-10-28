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
public abstract class Prop {
    
    //Su posición es actualizada en el mapa en sí, dado que todos los props tienen la misma velocidad en x y 0 en y
    //Añadir un collider y un método collide
    
    protected float[] position = new float[2];
    protected int[] collider = new int[2];
    
    public Prop(float[] pos, int[] col){
        
        position[0] = pos[0];
        position[1] = pos[1];
        
        collider[0] = col[0];
        collider[1] = col[1];
        
    }
    
    public boolean isColliding(Racer raz){
        
        int[] posRaz = raz.getPos();
        int[] colRaz = raz.getCollider();
        
        boolean xCol;
        xCol = (posRaz[0] + colRaz[0]) > (this.position[0] - this.collider[0]) || (posRaz[0] - colRaz[0]) < (this.position[0] + this.collider[0]);
        
        boolean yCol;
        yCol = (posRaz[1] + colRaz[1]) > (this.position[1] - this.collider[1]) || (posRaz[1] - colRaz[1]) < (this.position[1] + this.collider[1]);
        
        return (xCol && yCol);
    }
    
    public abstract void onCollision();
    
}
