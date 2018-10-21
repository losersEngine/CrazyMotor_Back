package losersengine.back.CrazyMotors_Back;

import org.springframework.web.socket.WebSocketSession;

public interface Function {
    public void ExecuteAction(String[] params,WebSocketSession session);
}
