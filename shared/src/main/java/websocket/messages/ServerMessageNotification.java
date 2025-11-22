package websocket.messages;

public class ServerMessageNotification extends ServerMessage {
    private String message;
    public ServerMessageNotification(ServerMessageType type, String message) {
        super(type);
        this.message = message;
    }
    public String getMessage(){
        return message;
    }
}
