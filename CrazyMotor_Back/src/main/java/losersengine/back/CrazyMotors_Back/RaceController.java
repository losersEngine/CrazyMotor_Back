package losersengine.back.CrazyMotors_Back;

import losersengine.back.CrazyMotors_Back.Objects.Racer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
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
    private AtomicInteger gameIds = new AtomicInteger(0);  //Sirve para dar el id a las salas
    private Gson gson = new Gson();
    
    private ConcurrentHashMap<Integer, RaceGame> salas;     //idSala y RaceGame
    private ConcurrentHashMap<Integer, Racer> sessions;      //idRacer y corredor

    //Diccionario de funciones
    private ConcurrentHashMap<String, Function> Funciones;
    
    public RaceController(){
        this.Funciones = new ConcurrentHashMap<>();
        this.sessions = new ConcurrentHashMap<>();
        this.salas = new ConcurrentHashMap<>();
        
        this.Funciones.put("ping", new Function(){                      //Funcion de ping comprobando que sigue habiendo conexión
            @Override
            public void ExecuteAction(String[] params, WebSocketSession session) {
                return;
            }
        });
        
        this.Funciones.put("unirSala", new Function(){
            @Override
            public void ExecuteAction(String[] params, WebSocketSession session) {      //Params: diff
                enterGame(Float.parseFloat(params[0]), session);
            }
        });
        
        this.Funciones.put("salirSala", new Function(){
            @Override
            public void ExecuteAction(String[] params, WebSocketSession session) {
                int idSala = (int) session.getAttributes().get(SALA_ATT);
                int idRacer = (int) session.getAttributes().get(RACER_ATT);
                
                RaceGame sala = salas.get(idSala);
                Racer raz = sessions.get(idRacer);
                
                synchronized(sala){
                    sala.removePlayer(raz);
                    
                    if(sala.getNum() == 0){
                        salas.remove(idSala);
                    }
                }
            }
        });
        
        this.Funciones.put("jumpPress", new Function(){                         //Params: true o false
            @Override
            public void ExecuteAction(String[] params, WebSocketSession session) {
                int racerId = (Integer) session.getAttributes().get(RACER_ATT);
                Racer r = sessions.get(racerId);
                
                r.setIsJumpPressed(Boolean.parseBoolean(params[0]));
            }
        });
        
        this.Funciones.put("nitroPress", new Function(){                        //Params: true o false
            @Override
            public void ExecuteAction(String[] params, WebSocketSession session) {
                int racerId = (Integer) session.getAttributes().get(RACER_ATT);
                Racer r = sessions.get(racerId);
                
                r.setIsNitroPressed(Boolean.parseBoolean(params[0]));
            }
        });
    
    }
    
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
            
        session.getAttributes().putIfAbsent(SALA_ATT, -1);
        session.getAttributes().putIfAbsent(RACER_ATT, -1);

    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
            
        try{

            String msg = message.getPayload();

            if (!msg.equals("undefined")){
                Instruccion i = gson.fromJson(msg, Instruccion.class);
                Function f = Funciones.get(i.getFuncion());

                Runnable tarea = () -> f.ExecuteAction(i.getParams(), session);                         //Cada tarea se ejecuta en un hilo
                executor.execute(tarea);
            }

        }catch (Exception e) {
            System.err.println("Exception processing message " + message.getPayload());
            e.printStackTrace(System.err);
        }
        

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {


            System.out.println("Connection closed. Session " + session.getId());

            RaceGame sala = null;
            int salaId;
            Racer razer = null;
            int racerId;

            //Cogemos ambos attribs
            salaId = (Integer) session.getAttributes().get(SALA_ATT);
            if (salaId != -1)
                sala = salas.get(salaId);
            racerId = (Integer) session.getAttributes().get(RACER_ATT);
            if (racerId != -1)
                razer = sessions.get(racerId);


            synchronized(sessions.values()){

            if(razer != null){

                //Quitamos el corredor de la sala y mandamos mensaje

                if(sala != null){

                    String[] vac = null;
                    this.Funciones.get("salirSala").ExecuteAction(vac, session);

                }

                //Quitamos el corredor de sesiones
                sessions.remove(razer.getId(), razer);

            }
        }

    }
    
    public void enterGame(float dif, WebSocketSession session){

        //TODO
        //Encontrar sala con esa diff ya creada y sólo un jugador
        //Si hay, meter a nuestro jugador y mandar a ambos un Socket de empezar
        //Si no hay, crear una sala, y mandar un Socket de esperar
        
        //Crear Razer a partir del numPlayers y LineHeights
        //pos = [0,sala.LineHeights(numPlayers.get())]
        
        for(Integer s : salas.keySet()){
                        
            RaceGame sg;

            sg = salas.get(s);

            synchronized(sg){

                if(sg != null && sg.getNum() == 1 && sg.getDifficulty() == dif){
                    
                    Racer raz = new Racer(racerIds.getAndIncrement(), new float[]{80.0f, RaceGame.LINE_HEIGHTS[1]}, session);
                    session.getAttributes().put(RACER_ATT, raz.getId());
                    sessions.put(raz.getId(), raz);
                    
                    sg.addRacer(raz);
                    session.getAttributes().put(SALA_ATT, sg.getId());
                    
                    return;
                }

            }

        }
        
        RaceGame gam = new RaceGame(gameIds.getAndIncrement(), dif);
        
        synchronized(gam){
            Racer raz = new Racer(racerIds.getAndIncrement(), new float[]{80.0f, RaceGame.LINE_HEIGHTS[0]}, session);
            session.getAttributes().put(RACER_ATT, raz.getId());
            sessions.put(raz.getId(), raz);

            gam.addRacer(raz);
            session.getAttributes().put(SALA_ATT, gam.getId());
            
            salas.put(gam.getId(), gam);
        }

    }
    
}
