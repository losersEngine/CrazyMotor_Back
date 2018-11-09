package losersengine.back.CrazyMotors_Back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@SpringBootApplication
@EnableWebSocket
public class RestPruebaIniApplication implements WebSocketConfigurer {
    
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ///////////////////Hemos empleado el puerto 7070 por estar ocupado en el pc de uno de nuestros integrantes////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args) {
		SpringApplication.run(RestPruebaIniApplication.class, args);
	}
        
        @Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		registry.addHandler(raceHandler(), "/race").setAllowedOrigins("*");
	}

	@Bean
	public WebSocketHandler raceHandler() {
            RaceController race = new RaceController();
            
            return race;
	}
}
