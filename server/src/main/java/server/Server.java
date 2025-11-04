package server;

import com.google.gson.Gson;
//import dataaccess.MemoryDataAccess;
import dataaccess.*;
import io.javalin.*;
import io.javalin.http.*;

import model.AuthData;
import model.GameData;
import model.UserData;

import service.GameService;
import service.UserService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDao;
    private final GameDAO gameDao;
    private final UserService userService;
    private final GameService gameService;
    private final AuthDAO authDao;

    public Server() {
        try {
            this.userDao = new SQLUserDAO();
            this.authDao = new SQLAuthDAO();
            this.gameDao = new SQLGameDAO();
        }
//        catch (DataAccessException e) {
//            throw new RuntimeException(e);
//        }
        catch (DataAccessException ex){
            throw new RuntimeException(ex);
        }


        this.userService = new UserService(userDao, authDao);
        this.gameService = new GameService(gameDao, authDao);
        //var userService = new UserService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

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
                throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.BadRequest, "username or email or password is null");
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
            if ((ex.getExc() == dataaccess.DataAccessException.PossibleExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //403
            if ((ex.getExc() == dataaccess.DataAccessException.PossibleExc.Forbidden)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(403).json(msg);
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
                throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.BadRequest, "username or password is null");
            }
            AuthData authData = userService.login(req);

            ctx.result(serializer.toJson(authData));
            ctx.status(200).result();
        }
        catch (dataaccess.DataAccessException ex){
            //400
            if ((ex.getExc() == dataaccess.DataAccessException.PossibleExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //401
            if (ex.getExc() == dataaccess.DataAccessException.PossibleExc.Unauthorized) {
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(401).json(msg);
            }
        }
//        //400
//        catch (DataAccessException ex){
//                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
//                ctx.status(400).json(msg);
//            }
//        //401
//        catch (DataAccessException ex){
//            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
//            ctx.status(401).json(msg);
//        }
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
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).json(msg);
        }
    }

    private void createGame(Context ctx){
        try {
            Gson serializer = new Gson();
            String authToken = ctx.header("authorization");
            String gameName = ctx.body();
            if (gameName.equals("{}")){
                throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.BadRequest, "gameName is emptyset or authToken is null");
            }
            if (authToken == null){
                throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.Unauthorized, "gameName is emptyset or authToken is null");
            }

            //Authenticate Authtoken
            userService.authenticatUser(authToken);
            //GameData gamedata = new GameData(null, null, null,gameName, null);
            GameData gameData = gameService.creategame(gameName);

            ctx.result(serializer.toJson(gameData));
            ctx.status(200).result();
        }
        //400
        catch (dataaccess.DataAccessException ex){
            if ((ex.getExc() == dataaccess.DataAccessException.PossibleExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //401
            if (ex.getExc() == dataaccess.DataAccessException.PossibleExc.Unauthorized) {
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(401).json(msg);
            }
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
            String json = serializer.toJson(Map.of("games", gameList));
            ctx.result(json);
            ctx.status(200).result();
        }
        //401
        catch (Exception ex){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(401).json(msg);
        }
    }



    //helper handler for joinGame
    private static class MultiVars {
        int gameID;
        String playerColor;
    }

    private void joinGame(Context ctx){
        try {
            Gson serializer = new Gson();
            String gameInfo = ctx.body();
            MultiVars info = serializer.fromJson(gameInfo,MultiVars.class);
            String authToken = ctx.header("authorization");

            int gameID = info.gameID;
            String playerColor = info.playerColor;


            if (gameInfo.equals("{}") || authToken == null || playerColor == null){
                throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.Unauthorized, "gameinfo is empty set or authToken is null or playerColor is null");
            }
            if ((!playerColor.equals("BLACK")) && (!playerColor.equals("WHITE"))){
                throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.Unauthorized, "player color is not black or white");
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
        catch (dataaccess.DataAccessException ex){
            //400
            if ((ex.getExc() == dataaccess.DataAccessException.PossibleExc.BadRequest)){
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(400).json(msg);
            }
            //401
            if (ex.getExc() == dataaccess.DataAccessException.PossibleExc.Unauthorized) {
                var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
                ctx.status(401).json(msg);
            }
        }
//        //400
//        catch (DataAccessException ex){
//            var errormsg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
//            ctx.status(400).json(errormsg);
//        }
//        //401
//        catch (DataAccessException ex){
//            var errormsg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
//            ctx.status(401).json(errormsg);
//        }
        //403
        catch (Exception ex){
            var errormsg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(403).json(errormsg);
        }
    }



    //hander clear application
    private void clearApplication(Context ctx) throws dataaccess.DataAccessException {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200);
        }
        catch (DataAccessException ex){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).json(msg);
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
