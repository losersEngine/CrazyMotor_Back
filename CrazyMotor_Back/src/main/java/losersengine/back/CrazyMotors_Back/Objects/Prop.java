package losersengine.back.CrazyMotors_Back.Objects;

/**
 *
 * @author brisin
 */
public abstract class Prop {
    
    //Su posición es actualizada en el mapa en sí, dado que todos los props tienen la misma velocidad en x y 0 en y
    //Añadir un collider y un método collide
    public final static int[] DIMENSIONS = new int[]{3100, 1860};
    
    protected float[] position = new float[2];
    protected int[] collider = new int[2];
    
    protected int state;
    protected String type;
    
    protected boolean toBreak;
    
    public Prop(float[] pos, int[] col){
        
        state = 0;
        
        toBreak = false;
        
        position[0] = pos[0];
        position[1] = pos[1];
        
        collider[0] = col[0];
        collider[1] = col[1];
        
    }
    
    public void update(float vel){
    
        this.position[0] = this.position[0] + vel;
        
        if(this.position[0] < -70.0f)
            this.toBreak = true;
        
    }
    
    public boolean isColliding(Racer raz){
        
        boolean coll = this.state == 0; //Sólo se puede colisionar con objetos que estén en state 0 (Caja sin romper, láser activo...)
        
        float[] posRaz = raz.getPos();
        int[] colRaz = raz.getCollider();
        
        float dis = Math.abs(posRaz[0] - this.position[0]);
        boolean xCol = false;
        boolean yCol = false;
        
        if(dis < 100 && coll){
            
            yCol = (posRaz[1] > (this.position[1] + this.collider[1])) && ((posRaz[1] + colRaz[1]) < this.position[1]);
        
            if(yCol)
                xCol = ((posRaz[0] + colRaz[0]) > (this.position[0] - this.collider[0])) && ((posRaz[0] - colRaz[0]) < (this.position[0] + this.collider[0]));

        }
        
        return (xCol && yCol);
    }
    
    public abstract void onCollision(Racer raz);

    public boolean isToBreak() {
        return toBreak;
    }

    public float[] getPosition() {
        return position;
    }

    public int getState() {
        return state;
    }

    public String getType() {
        return type;
    }
    
    public int getWidth(){
        return collider[0] * 2;
    }
    
}
