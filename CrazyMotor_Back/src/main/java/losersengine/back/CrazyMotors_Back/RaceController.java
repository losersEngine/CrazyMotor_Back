/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package losersengine.back.CrazyMotors_Back;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 *
 * @author Brisin
 */
public class RaceController extends TextWebSocketHandler {
    
    private static final Executor executor = Executors.newCachedThreadPool();

    private static final String RACER_ATT = "racer";
    private static final String SALA_ATT = "sala";


    private ObjectMapper mapper = new ObjectMapper();
    private AtomicInteger racerIds = new AtomicInteger(0);  //Sirve para dar el id a los corredores
    private Gson gson = new Gson();
    
    private ConcurrentHashMap<String, RaceGame> salas;     //idSala y RaceGame
    private ConcurrentHashMap<String, Racer> sessions;      //idSnake y corredor

    //Diccionario de funciones
    private ConcurrentHashMap<String, Function> Funciones;
    
    public RaceController(){
        this.Funciones = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
        this.salas = new ConcurrentHashMap<>();
        
        //TODO
        this.Funciones.put("unirGame", new Function(){
            @Override
            public void ExecuteAction(String[] params, WebSocketSession session) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        });
    
    }
    
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            
        session.getAttributes().putIfAbsent(SALA_ATT, "none");

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        try{

            String msg = message.getPayload();

            Instruccion i = gson.fromJson(msg, Instruccion.class);
            Function f = Funciones.get(i.getFuncion());
            System.out.println(i.getFuncion() + " " + i.getParams());

            Runnable tarea = () -> f.ExecuteAction(i.getParams(), session);                         //Cada tarea se ejecuta en un hilo
            executor.execute(tarea);

        }catch (Exception e) {
            System.err.println("Exception processing message " + message.getPayload());
            e.printStackTrace(System.err);
        }


    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {


            System.out.println("Connection closed. Session " + session.getId());

            String s;
            Racer razer;
            String name;

            //Cogemos ambos attribs
            s = (String) session.getAttributes().get(SALA_ATT);
            razer = (Racer) session.getAttributes().get(RACER_ATT);


            synchronized(sessions.values()){

            if(razer != null){

                name = razer.getName();
                //Quitamos el corredor de la sala y mandamos mensaje

                if(!s.equals("none")){

                    String[] vac = null;
                    this.Funciones.get("salirSala").ExecuteAction(vac, session);

                }


                //Mensaje desconexión
                ObjectNode difusion = mapper.createObjectNode();
                difusion.put("type","jugadorDesconecta");
                difusion.put("name", name);

                //Quitamos el corredor de sesiones
                sessions.remove(razer.getName(), razer);

                //Mandamos mensaje, ya sincronizado en sendmessage
                for(Racer rzr : sessions.values()){
                    try {
                        rzr.sendMessage(difusion.toString());
                    } catch (Exception ex) {
                        Logger.getLogger(RaceController.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }

            }
        }

    }
    
    public void enterGame(long dif){

        //TODO
        //Encontrar sala con esa diff ya creada y sólo un jugador
        //Si hay, meter a nuestro jugador y mandar a ambos un Socket de empezar
        //Si no hay, crear una sala, y mandar un Socket de esperar

    }
    
}
