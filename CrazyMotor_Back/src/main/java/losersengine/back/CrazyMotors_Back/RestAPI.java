package losersengine.back.CrazyMotors_Back;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author Brisin
 */
@RestController
public class RestAPI {
    
    private static RaceController handler;
    //private final Gson gson = new Gson();
    
    private final ObjectMapper mapper = new ObjectMapper();
    
    public static void setRaceController(RaceController race){
        handler = race;
    }
    
    @RequestMapping(value = "/newGame", method = RequestMethod.POST)
    public void crearNuevaPartida(@RequestBody String nameGame){

        try {
            JsonNode node = mapper.readTree(nameGame);
            
            handler.enterGame(node.get("dif").asLong());
        } catch (IOException ex) {
            Logger.getLogger(RestAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
