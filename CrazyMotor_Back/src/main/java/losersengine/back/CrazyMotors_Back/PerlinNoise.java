package losersengine.back.CrazyMotors_Back;

import java.util.Random;

/**
 *
 * @author brisin
 */
public class PerlinNoise {
    
    private final float AVANCE = 0.5f;
    private final float Amplitude = 50.0f;
    
    private Random rnd;
    
    private int M,A,C,Z; //Z = seed
    
    private float a, b;
    
    public PerlinNoise(){
        rnd = new Random(System.currentTimeMillis());
        
        M = 4294967;
        A = 166456;
        C = 1;
        
        Z = (int) Math.floor(rnd.nextDouble() * M);
    
    
        a = rand();
        b = rand();
    }
    
    private float rand(){
        Z = (A * Z + C) % M;
        return Z / M;
    }
    
    private float interpolate(float pa, float pb, float px){
        float ft = (float) (px * Math.PI);
        float f = (float) ((1.0f - Math.cos(ft)) * 0.5f);
        
        return pa * (1-f) + pb * f;
    }
    
    public float getValue(float x){
        //y = h/2 + interpolate(a,b, (x % wl) / wl) * amp;
        //h=100, amplitud = 50;
        return 50 + interpolate(a, b, x) * Amplitude;
    }
    
}
