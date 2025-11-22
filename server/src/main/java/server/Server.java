package server;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import dataaccess.DataAccessException;
import io.javalin.*;
import io.javalin.http.*;

import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsMessageContext;
import model.*;

import service.GameService;
import service.UserService;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

import websocket.commands.UserGameCommand;
import websocket.messages.ServerMessage;
import websocket.messages.ServerMessageError;
import websocket.messages.ServerMessageLoadGame;
import websocket.messages.ServerMessageNotification;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDao;
    private final GameDAO gameDao;
    private final UserService userService;
    private final GameService gameService;
    private final AuthDAO authDao;

    //list of players
    private List<WsMessageContext> playerList = new ArrayList<>();

    public Server() {
        try {
            this.userDao = new SQLUserDAO();
            this.authDao = new SQLAuthDAO();
            this.gameDao = new SQLGameDAO();
        }
        catch (DataAccessException ex){
            throw new RuntimeException(ex);
        }


        this.userService = new UserService(userDao, authDao);
        this.gameService = new GameService(gameDao, authDao);

        javalin = Javalin.create(config -> config.staticFiles.add("web"))
                .ws("/ws", ws -> {
                    ws.onConnect(this::connect);
                    ws.onMessage(this::onMessage);
                    ws.onClose(ctx -> System.out.println("Websocket closed"));
                });




        // Register your endpoints and exception handlers here.
        //line 1
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGame);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.delete("/db", this::clearApplication);

    }
    //websocket methds
    private void connect(WsConnectContext wsConnectContext) {
        System.out.println("Websocket Connected");
        wsConnectContext.enableAutomaticPings();
    }
    //ctx -> ctx.send("Websocket response:" + ctx.message()
    private void onMessage(WsMessageContext wsMessageContext) throws Exception {
        removingClosedPlayers();
        //deserializes the wsMessageContext.message()
        UserGameCommand userGameCommandObj = new Gson().fromJson(wsMessageContext.message(), UserGameCommand.class);
        //get the UserGameCommand
        UserGameCommand.CommandType userGameCommand = userGameCommandObj.getCommandType();
        //sort based on userGameCommand
        if (userGameCommand == UserGameCommand.CommandType.CONNECT){
            //replace this with the real chess game eventually
            //int gameID, String playerColor, String username
            try {
                GameData gameData = gameService.updategame(userGameCommandObj.getGameID(), "", null);
                String username = userService.getUsernameAuth(userGameCommandObj.getAuthToken());
                ChessGame game = gameData.game();
                ServerMessageLoadGame serverMessage = new ServerMessageLoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game);
                String serverSent = new Gson().toJson(serverMessage);
                wsMessageContext.send(serverSent);
                if (!playerList.isEmpty()) {
                    //replace this with a way to get username
                    String message = "join otherPlayer color";
                    everyoneExceptCurPlayer(wsMessageContext, message);

                }
                playerList.add(wsMessageContext);
            }
            catch(Exception e){
                System.out.println(e.getMessage());
                String errorMessage = e.getMessage();
                ServerMessageError serverMessageError = new ServerMessageError(ServerMessage.ServerMessageType.ERROR, errorMessage);
                String serverSent = new Gson().toJson(serverMessageError);
                wsMessageContext.send(serverSent);
            }
        }
        //send a serialized server message
        //wsMessageContext.send("Server side: Websocket response:" + wsMessageContext.message());
        //wsMessageContext.send("LOAD_GAME");
    }
    private void everyoneExceptCurPlayer(WsMessageContext wsMessageContext, String message){
        for (WsMessageContext player : playerList){
            if (!player.equals(wsMessageContext)){
                notification(player, message);
            }
        }
    }

    private void removingClosedPlayers() {
        List<WsMessageContext> removalPlayerList = new ArrayList<>();
        for (WsMessageContext player : playerList) {
            if (!player.session.isOpen()) {
                removalPlayerList.add(player);
            }
        }
        for (WsMessageContext player : removalPlayerList){
            playerList.remove(player);
        }
    }

    private void notification(WsMessageContext wsMessageContext, String message){
        ServerMessageNotification serverMessage = new ServerMessageNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        String serverSent = new Gson().toJson(serverMessage);
        wsMessageContext.send(serverSent);
    }



// handler register (whole method)
    private void register(Context ctx) {
        //req = request, res = response
        try {
            Gson serializer = new Gson();
            //line 2
            //reads Json req body
            String reqJson = ctx.body();
            UserData req = serializer.fromJson(reqJson, UserData.class);
            //pass back an AuthData
            if (req.username() == null || req.email() == null || req.password() == null){
                throw new DataAccessException(DataAccessException.PosExc.BadRequest, "username or email or password is null");
            }
            // call to the service and register
            //line 3
            AuthData authData = userService.register(req);
            //line 13
            ctx.result(serializer.toJson(authData));
            ctx.status(200).result();
        }
        //400
        catch (DataAccessException ex){
            //400
            if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //403
            if ((ex.getExc() == DataAccessException.PosExc.Forbidden)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(403).json(msg);
            }
            //500
            if ((ex.getExc() == DataAccessException.PosExc.ServerError)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(500).json(msg);
            }
        }
        //403
        catch (Exception ex){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            //ctx.status(403).result();
            ctx.status(403).json(msg);
        }
    }
    private void login(Context ctx){
        try{
            Gson serializer = new Gson();
            String reqJson = ctx.body();
            UserData req = serializer.fromJson(reqJson, UserData.class);
            if (req.username() == null || req.password() == null){
                throw new DataAccessException(DataAccessException.PosExc.BadRequest, "username or password is null");
            }
            AuthData authData = userService.login(req);

            ctx.result(serializer.toJson(authData));
            ctx.status(200).result();
        }
        catch (DataAccessException ex){
            caseBundleBadUnauthServer(ctx, ex);
        }
        //403
        catch (Exception ex){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).json(msg);
        }
    }
    private void logout(Context ctx) {
        try {
            Gson serializer = new Gson();
            String authToken = ctx.header("authorization");
            AuthData authdata = new AuthData(authToken,null);
            userService.logout(authdata);
            ctx.status(200).result();
        }
        //401
        catch (DataAccessException ex){
            catchBundleUnauthServer(ctx, ex);
        }

    }
    public void catchBundleUnauthServer(Context ctx, DataAccessException ex){
        //401
        if ((ex.getExc() == DataAccessException.PosExc.Unauthorized)) {
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).json(msg);
        }
        //500
        if ((ex.getExc() == DataAccessException.PosExc.ServerError)){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).json(msg);
        }
    }

    private void createGame(Context ctx){
        try {
            Gson serializer = new Gson();
            String authToken = ctx.header("authorization");
            String gameName = ctx.body();
            if (gameName.equals("{}")){
                throw new DataAccessException(DataAccessException.PosExc.BadRequest, "gameName is emptyset or authToken is null");
            }
            if (authToken == null){
                throw new DataAccessException(DataAccessException.PosExc.Unauthorized, "gameName is emptyset or authToken is null");
            }

            //Authenticate Authtoken
            userService.authenticatUser(authToken);
            //GameData gamedata = new GameData(null, null, null,gameName, null);
            GameData gameData = gameService.creategame(gameName);

            ctx.result(serializer.toJson(gameData));
            ctx.status(200).result();
        }

        catch (DataAccessException ex){
            caseBundleBadUnauthServer(ctx, ex);
        }
    }
    //bundle of catch statements
    public void caseBundleBadUnauthServer(Context ctx, DataAccessException ex){
        //400
        if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).json(msg);
        }
        //401
        if (ex.getExc() == DataAccessException.PosExc.Unauthorized) {
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).json(msg);
        }
        //500
        if ((ex.getExc() == DataAccessException.PosExc.ServerError)){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(500).json(msg);
        }
    }

    private void listGame(Context ctx){
        try {
            Gson serializer = new Gson();
            String authToken = ctx.header("authorization");
            //Authenticate Authtoken
            userService.authenticatUser(authToken);

            //GameData gamedata = new GameData(null, null, null,gameName, null);
            HashMap<Integer, GameData> games = gameService.listgames();

            Collection<GameData> gameList = games.values();
            //Map.of("games", gameList) -> GameList
            String json = serializer.toJson(new GameList(gameList));
            ctx.result(json);
            ctx.status(200).result();
        }
        //401
        catch (DataAccessException ex){
            catchBundleUnauthServer(ctx, ex);
        }

    }




    private void joinGame(Context ctx){
        try {
            Gson serializer = new Gson();
            String gameInfo = ctx.body();
            JoinGameData info = serializer.fromJson(gameInfo, JoinGameData.class);
            String authToken = ctx.header("authorization");

            int gameID = info.gameID();
            String playerColor = info.playerColor();


            if (gameInfo.equals("{}") || authToken == null || playerColor == null){
                throw new DataAccessException(DataAccessException.PosExc.BadRequest,"gameinfo{}||authToken,playerColor null");
            }
            if ((!playerColor.equals("BLACK")) && (!playerColor.equals("WHITE"))){
                throw new DataAccessException(DataAccessException.PosExc.BadRequest, "player color is not black or white");
            }

            //Authenticate Authtoken
            userService.authenticatUser(authToken);

            //get username
            String username = userService.getUsernameAuth(authToken);

            //Update
            GameData updatedGameData = gameService.updategame(gameID, playerColor, username);
            ctx.result(serializer.toJson(updatedGameData));
            ctx.status(200).result();

        }
        catch (DataAccessException ex){
            //400
            if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //401
            if (ex.getExc() == DataAccessException.PosExc.Unauthorized) {
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(401).json(msg);
            }
            //403
            if (ex.getExc() == DataAccessException.PosExc.Forbidden) {
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(403).json(msg);
            }
            //500
            if ((ex.getExc() == DataAccessException.PosExc.ServerError)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(500).json(msg);
            }
        }

//        }
        //403
        catch (Exception ex){
            var errormsg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).json(errormsg);
        }
    }



    //hander clear application
    public void clearApplication(Context ctx) throws DataAccessException {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200);
        }
        catch (DataAccessException ex){
            //400
            if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //500
            if ((ex.getExc() == DataAccessException.PosExc.ServerError)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(500).json(msg);
            }
//            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
//            ctx.status(400).json(msg);
        }
    }



    public int run(int desiredPort) {


        javalin.start(desiredPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
}
