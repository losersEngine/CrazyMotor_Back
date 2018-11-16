package test;

import java.io.IOException;
import java.net.URISyntaxException;
import static org.junit.Assert.assertTrue;
import java.util.concurrent.atomic.AtomicReference;
import javax.websocket.DeploymentException;
import losersengine.back.CrazyMotors_Back.IniApplication;
import org.junit.BeforeClass;
import org.junit.Test;

public class CrazyTest {
    
        @BeforeClass
	public static void startServer() throws IOException{
            IniApplication.main(new String[]{ "--server.port=7070" });
        }
		
        /////////////////////////////////////////////////////////////////////////////////
        //@Test
        public void testInicioAutom() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
            
            AtomicReference<String> firstMsg = new AtomicReference<>();
            WebSocketClient wsc[] = new WebSocketClient[2];
            
            wsc[0] = new WebSocketClient();
            wsc[0].onMessage((session, msg) -> {
                //System.out.println("TestMessage: "+msg);
            });
                
            wsc[1] = new WebSocketClient();
            wsc[1].onMessage((session, msg) -> {
                firstMsg.set(msg);
            });
            
            String nmsg = "{\"funcion\": \"unirSala\", \"params\": [\"1\"]}";
            //Conectar 2 jugadores
            for(int i = 0; i < 2; i++){
                wsc[i].connect("wss://127.0.0.1:7070/race");
                wsc[i].sendMessage(nmsg);
            }

            System.out.println("Connected");
            Thread.sleep(5000);

            //Comprobar el mensaje
            String msg = firstMsg.get();
            assertTrue("The message should contain 'update', but it is "+msg, msg.contains("update"));

                    
            wsc[0].disconnect();
            Thread.sleep(2000);
            
            
            msg = firstMsg.get();
            assertTrue("The message should contain 'finPartida', but it is "+msg, msg.contains("finPartida"));
            
            wsc[1].disconnect();
            
        }
        
        //@Test
        public void testCarga() throws DeploymentException, IOException, URISyntaxException, InterruptedException {
            
            Thread.sleep(9000);
            
            AtomicReference<String> firstMsg = new AtomicReference<>();
            WebSocketClient wsc[] = new WebSocketClient[10];
            
            for(int i = 0; i < wsc.length-1; i++){
                wsc[i] = new WebSocketClient();
                wsc[i].onMessage((session, msg) -> {
                    //System.out.println("TestMessage: "+msg);
                });
            }
            
            
                
            wsc[9] = new WebSocketClient();
            wsc[9].onMessage((session, msg) -> {
                firstMsg.set(msg);
            });
            
            String nmsg = "{\"funcion\": \"unirSala\", \"params\": [\"1\"]}";
            //Conectar 2 jugadores
            for(int i = 0; i < wsc.length; i++){
                wsc[i].connect("wss://127.0.0.1:7070/race");
                wsc[i].sendMessage(nmsg);
            }

            System.out.println("Connected");
            Thread.sleep(5000);

            //Comprobar el mensaje
            String msg = firstMsg.get();
            assertTrue("The message should contain 'update', but it is "+msg, msg.contains("update"));

            for(int i = 0; i < wsc.length-1; i++){
                wsc[i].disconnect();
            }

            Thread.sleep(4000);
            wsc[9].disconnect();
            Thread.sleep(2000);
            
            msg = firstMsg.get();
            assertTrue("The message should contain 'finPartida', but it is "+msg, msg.contains("finPartida"));
            
        }
        
}