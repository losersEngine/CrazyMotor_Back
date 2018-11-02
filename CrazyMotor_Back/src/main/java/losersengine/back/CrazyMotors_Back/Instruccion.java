package losersengine.back.CrazyMotors_Back;

/**
 *
 * @author Brisin
 */
public class Instruccion {
    
    private String funcion;
    private String[] params;
    
    public Instruccion(String t, String[] p){
    
        this.funcion = t;
        this.params = p;
    
    }

    public String getFuncion() {
        return funcion;
    }

    public void setFuncion(String funcion) {
        this.funcion = funcion;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] param) {
        for(int i = 0; i < param.length; i++){
            this.params[i] = param[i];
        }
    }
    
    
}
