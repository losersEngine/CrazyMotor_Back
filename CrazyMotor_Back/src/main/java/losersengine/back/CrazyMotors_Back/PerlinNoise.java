package losersengine.back.CrazyMotors_Back;

import java.util.Random;

/**
 *
 * @author brisin
 */
public class PerlinNoise {
    
    private final float Amplitude = 100.0f;
    private final float WaveLength = 10.0f;
    
    private Random rnd;
    
    private float a, b;
    
    public PerlinNoise(int seed){
        rnd = new Random(seed);
    
        a = rand();
        b = rand();
    }
    
    private float rand(){
        return rnd.nextFloat() -0.5f;
    }
    
    private float interpolate(float pa, float pb, float px){
        float ft = (float) (px * Math.PI);
        float f = (float) ((1.0f - Math.cos(ft)) * 0.5f);
        
        return pa * (1-f) + pb * f;
    }
    
    public float getValue(float x){
        //y = h/2 + interpolate(a,b, (x % wl) / wl) * amp;
        //h=100, amplitud = 50;
        //float value = 50.0f + interpolate(a, b, x) * Amplitude;
        
        float y;
        
        if(x % WaveLength == 0){
            a = b;
            b = rand();
            y = 50.0f + a * Amplitude;
        }else{
            y = 50.0f + interpolate(a, b, (x % WaveLength) / WaveLength) * Amplitude;
        }
        
        return y;
    }
    
}
