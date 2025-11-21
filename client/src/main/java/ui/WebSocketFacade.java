package ui;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import ui.exception.ClientException;

import websocket.commands.UserGameCommand;

import java.io.IOException;
import java.net.URI;


public class WebSocketFacade extends Endpoint{
    public Session session;
    public LoginClient loginClient;

    public WebSocketFacade(String url, LoginClient loginClient) throws ClientException {
        try {
            //modifies url for websocket
            url = url.replace("http", "ws");
            URI wsURI = new URI(url + "/ws");
            this.loginClient = loginClient;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, wsURI);

            this.session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    //change this  to serialization and sending of notify(notification)
                    System.out.println(message);
                    System.out.println("\nEnter another message you want to echo:");
                }
            });
        }
        catch (Exception e){
            throw new ClientException("issue with WebSocketFacade", e);
        }
    }

    public void send(UserGameCommand.CommandType connect, String authToken, int gameID) throws ClientException {
        try {
            UserGameCommand userGameCommand = new UserGameCommand(connect,authToken, gameID);
            session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
        }
        catch(IOException e){
            throw new ClientException("IOException issue in send()", e);
        }
    }
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig){
    }


}
