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
    
    public void update(int vel){
    
        this.position[0] = this.position[0] + vel;
        
    }
    
    public boolean isColliding(Racer raz){
        
        int[] posRaz = raz.getPos();
        int[] colRaz = raz.getCollider();
        
        int[] vec = new int[2];
        vec[0] = (int) (posRaz[0] - this.position[0]);
        vec[1] = (int) (posRaz[1] - this.position[1]);
        
        int dis = (int) Math.sqrt(Math.exp(vec[0]) + Math.exp(vec[1]));
        boolean xCol = false;
        boolean yCol = false;
        
        if(dis < 100){
            
            yCol = (posRaz[1] > this.position[1]) && (posRaz[1] < (this.position[1] + this.collider[1])) ||
                    ((posRaz[1] + colRaz[1]) > this.position[1]) && ((posRaz[1] + colRaz[1]) < (this.position[1] + this.collider[1])) ||
                    (posRaz[1] > this.position[1]) && ((posRaz[1] + colRaz[1]) < (this.position[1] + this.collider[1])) ||
                    (posRaz[1] < this.position[1]) && ((posRaz[1] + colRaz[1]) > (this.position[1] + this.collider[1]));
        
            if(yCol)
                xCol = ((posRaz[0] + colRaz[0]) > (this.position[0] - this.collider[0]) && (posRaz[0] - colRaz[0]) < (this.position[0] - this.collider[0])) ||
                        ((posRaz[0] - colRaz[0]) > (this.position[0] - this.collider[0]) && (posRaz[0] - colRaz[0]) < (this.position[0] + this.collider[0])) ||
                        ((posRaz[0] - colRaz[0]) > (this.position[0] - this.collider[0]) && (posRaz[0] + colRaz[0]) < (this.position[0] + this.collider[0])) ||
                        ((posRaz[0] - colRaz[0]) < (this.position[0] - this.collider[0]) && (posRaz[0] + colRaz[0]) > (this.position[0] + this.collider[0]));

        }
        
        return (xCol && yCol);
    }
    
    public abstract void onCollision(Racer raz);
    
}
