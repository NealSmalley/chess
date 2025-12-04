package ui;

import chess.ChessGame;
import chess.ChessMove;
import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.WebSocketContainer;
import ui.exception.ClientException;

import ui.server.PrintBoard;
import websocket.commands.LeaveCommand;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageLoadGame;
import websocket.messages.ServerMessageNotification;

import java.io.IOException;
import java.net.URI;


public class WebSocketFacade extends Endpoint{
    public Session session;
    public LoginClient loginClient;
    private boolean checkMateOrStale = false;

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
                    //deserialize server message
                    ServerMessage serverMessage = new Gson().fromJson(message, ServerMessage.class);
                    ServerMessage.ServerMessageType messageType = serverMessage.getServerMessageType();
                    //receives the message back from the server
                    if (messageType.equals(ServerMessage.ServerMessageType.LOAD_GAME)){
                        System.out.println("Load_Game");
                        ServerMessageLoadGame loadGameMessage = new Gson().fromJson(message, ServerMessageLoadGame.class);
                        ChessGame game = loadGameMessage.game();
                        PrintBoard board = new PrintBoard();
                        if (LoginClient.color != null) {
                            board.printBoard(game, LoginClient.color);
                        }
                        //observer white view
                        else if (LoginClient.color == null){
                            board.printBoard(game,"white");
                        }
                    }
                    else if (messageType.equals(ServerMessage.ServerMessageType.NOTIFICATION)){
                        ServerMessageNotification notificationMessage = new Gson().fromJson(message, ServerMessageNotification.class);
                        String messageReceived = notificationMessage.getMessage();
                        System.out.println("Notification: "+ messageReceived);
                        //checkmate or stalemate
                        if (messageReceived.contains("checkmate") || messageReceived.contains("stalemate")){
                            checkMateOrStale = true;
                        }
                    }
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
    public void sendLeave(UserGameCommand.CommandType leave, String authToken, int gameID, String color) throws ClientException {
        try {
            UserGameCommand userGameCommand = new LeaveCommand(leave,authToken, gameID, color);
            session.getBasicRemote().sendText(new Gson().toJson(userGameCommand));
        }
        catch(IOException e){
            throw new ClientException("IOException issue in send()", e);
        }
    }
    public void sendMakeMove(UserGameCommand.CommandType makemove, String authToken, int gameID,ChessMove makeMove) throws ClientException {
        if (checkMateOrStale){
            throw new ClientException("You can't make moves after the game is over");
        }
        try {
            UserGameCommand userGameCommand = new MakeMoveCommand(makemove,authToken, gameID, makeMove);
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
