package server;
import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
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
import java.util.*;
import websocket.commands.MakeMoveCommand;
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
    private Map<Integer, List<WsMessageContext>> gameMap = new HashMap<>();
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
        //line 1
        javalin.post("/user", this::register);
        javalin.post("/session", this::login);
        javalin.delete("/session", this::logout);
        javalin.get("/game", this::listGame);
        javalin.post("/game", this::createGame);
        javalin.put("/game", this::joinGame);
        javalin.delete("/db", this::clearApplication);
    }
    private void connect(WsConnectContext wsConnectContext) { //websocket methods
        System.out.println("Websocket Connected");
        wsConnectContext.enableAutomaticPings();
    }
    private void onMessage(WsMessageContext wsMessageContext) throws Exception {
        removingClosedPlayers();
        UserGameCommand userGameCommandObj = new Gson().fromJson(wsMessageContext.message(), UserGameCommand.class);
        UserGameCommand.CommandType userGameCommand = userGameCommandObj.getCommandType();//get the UserGameCommand
        if (userGameCommand == UserGameCommand.CommandType.CONNECT){//sort based on userGameCommand
            connect(userGameCommandObj, wsMessageContext);
        }
        else if ((userGameCommand == UserGameCommand.CommandType.MAKE_MOVE)){
            makeMethod(userGameCommandObj, wsMessageContext);
        }
        else if (userGameCommand == UserGameCommand.CommandType.RESIGN){
            resign(userGameCommandObj, wsMessageContext);
        }
        else if (userGameCommand == UserGameCommand.CommandType.LEAVE){
            leave(userGameCommandObj, wsMessageContext);
        }
    }
    private boolean checkMateChecker(ChessGame game, GameData updated, List<WsMessageContext> listGamePlayers, WsMessageContext wsMessageContext){
        if (game.isInCheckmate(ChessGame.TeamColor.WHITE)){
            String checkUser = updated.whiteUsername();
            String checkMessage = checkUser + " is in checkmate. Game Over.";
            everyonePlayerNotification(listGamePlayers,wsMessageContext, checkMessage);
            return true;
        }
        if (game.isInCheckmate(ChessGame.TeamColor.BLACK)){
            String checkUser = updated.blackUsername();
            String checkMessage = checkUser + " is in checkmate. Game Over.";
            everyonePlayerNotification(listGamePlayers,wsMessageContext, checkMessage);
            return true;
        }
        else{
            return false;
        }
    }
    private boolean checkChecker(ChessGame game, GameData gameDataUpdated, List<WsMessageContext> listGamePlayers, WsMessageContext wsMessageContext){
        if (game.isInCheck(ChessGame.TeamColor.WHITE)){
            String checkUser = gameDataUpdated.whiteUsername();
            String checkMessage = checkUser + " is in check";
            everyonePlayerNotification(listGamePlayers,wsMessageContext, checkMessage);
            return true;
        }
        if (game.isInCheck(ChessGame.TeamColor.BLACK)){
            String checkUser = gameDataUpdated.blackUsername();
            String checkMessage = checkUser + " is in check";
            everyonePlayerNotification(listGamePlayers,wsMessageContext, checkMessage);
            return true;
        }
        else{
            return false;
        }
    }
    private boolean staleChecker(ChessGame game, GameData updated, List<WsMessageContext> listGamePlayers, WsMessageContext wsMessageContext) {
        if (game.isInStalemate(ChessGame.TeamColor.WHITE)){
            String staleUser = updated.whiteUsername();
            String staleMessage = staleUser + " is in stalemate";
            everyonePlayerNotification(listGamePlayers,wsMessageContext, staleMessage);
            return true;
        }
        if (game.isInStalemate(ChessGame.TeamColor.BLACK)){
            String staleUser = updated.blackUsername();
            String staleMessage = staleUser + " is in stalemate";
            everyonePlayerNotification(listGamePlayers,wsMessageContext, staleMessage);
            return true;
        }
        else{
            return false;
        }
    }
    private String toChessNotation(ChessPosition coordinates){
        int row = coordinates.getRow();
        int col = coordinates.getColumn();
        String column = switch(col){
            case 1 -> "a";
            case 2 -> "b";
            case 3 -> "c";
            case 4 -> "d";
            case 5 -> "e";
            case 6 -> "f";
            case 7 -> "g";
            case 8 -> "h";
            default -> Integer.toString(col);
        };
        return row+column;
    }
    private void hasResigned(boolean hasResigned) throws InvalidMoveException{
        if (hasResigned){
            throw new InvalidMoveException("You can't move after you resigned from the game");
        }
    }
    private boolean invalidMoveOpp(String username, GameData gameData, ChessGame game) throws InvalidMoveException{
        if (username.equals(gameData.whiteUsername()) && (game.getTeamTurn() == ChessGame.TeamColor.WHITE)){
            return true;
        } else if (username.equals(gameData.blackUsername()) && (game.getTeamTurn() == ChessGame.TeamColor.BLACK)){
            return true;
        }
        else{
            throw new InvalidMoveException("You can't play your opponents pieces");
        }
    }
    private void errorMessageSender(Exception e, WsMessageContext wsMessageContext){
        System.out.println(e.getMessage());
        String errorMessage = e.getMessage();
        ServerMessageError serverMessageError = new ServerMessageError(ServerMessage.ServerMessageType.ERROR, errorMessage);
        String serverSent = new Gson().toJson(serverMessageError);
        wsMessageContext.send(serverSent);
    }
    private void everyoneExceptCurPlayer(Integer gameID, WsMessageContext wsMessageContext, String message){
        List<WsMessageContext> gamePlayerList = gameMap.get(gameID);
        for (WsMessageContext player : gamePlayerList){
            if (!player.equals(wsMessageContext)){ //if in game map
                notification(player, message);
            }
        }
    }
    private void everyonePlayerLoadGame(List<WsMessageContext> listGamePlayers, ChessGame game){
        for (WsMessageContext player : listGamePlayers){
                loadGameSender(player, game);
        }
    }
    private void everyonePlayerNotification(List<WsMessageContext> listGamePlayers, WsMessageContext wsMessageContext, String message){
        for (WsMessageContext player : listGamePlayers){
            notification(player, message);
        }
    }
    private void removingClosedPlayers() {
        for (int gameID : gameMap.keySet()) {
            if (gameMap.get(gameID) != null) {
                List<WsMessageContext> listGamePlayers = gameMap.get(gameID);
                List<WsMessageContext> removalPlayerList = new ArrayList<>();
                for (WsMessageContext player : listGamePlayers) {
                    if (!player.session.isOpen()) {
                        removalPlayerList.add(player);
                    }
                }
                for (WsMessageContext player : removalPlayerList) {
                    listGamePlayers.remove(player);
                }
            }
        }
    }
    private void removingAfterLeave(List<WsMessageContext> listGamePlayers, WsMessageContext wsMessageContext){
        List<WsMessageContext> removalPlayerList = new ArrayList<>();
        for (WsMessageContext player : listGamePlayers) {
            if (player.equals(wsMessageContext)){
                removalPlayerList.add(player);
            }
        }
        for (WsMessageContext player : removalPlayerList){
            listGamePlayers.remove(player);
        }
    }
    private void notification(WsMessageContext wsMessageContext, String message){
        ServerMessageNotification serverMessage = new ServerMessageNotification(ServerMessage.ServerMessageType.NOTIFICATION, message);
        String serverSent = new Gson().toJson(serverMessage);
        wsMessageContext.send(serverSent);
    }
    private void loadGameSender(WsMessageContext wsMessageContext, ChessGame game){
        ServerMessageLoadGame serverMessageLoadGame = new ServerMessageLoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game);
        String serverSentLoadGame = new Gson().toJson(serverMessageLoadGame);
        wsMessageContext.send(serverSentLoadGame);
    }
    private void register(Context ctx) {
        try {
            Gson serializer = new Gson();
            String reqJson = ctx.body();//line 2  //reads Json req body
            UserData req = serializer.fromJson(reqJson, UserData.class);
            if (req.username() == null || req.email() == null || req.password() == null){ //pass back an AuthData
                throw new DataAccessException(DataAccessException.PosExc.BadRequest, "username or email or password is null");
            }
            AuthData authData = userService.register(req);  //line 3
            ctx.result(serializer.toJson(authData));//line 13
            ctx.status(200).result();
        }
        catch (DataAccessException ex){
            if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){    //400
                errorMessage(400, ctx, ex);
            }
            if ((ex.getExc() == DataAccessException.PosExc.Forbidden)){     //403
                errorMessage(403, ctx, ex);
            }
            if ((ex.getExc() == DataAccessException.PosExc.ServerError)){   //500
                errorMessage(500, ctx, ex);
            }
        }
        catch (Exception ex){ //403
            errorMessage(403, ctx, ex);
        }
    }
    private void errorMessage(int status, Context ctx, Exception ex){
        var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
        ctx.status(status).json(msg);
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
        catch (Exception ex){ //403
            errorMessage(403, ctx, ex);
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
        catch (DataAccessException ex){ //401
            catchBundleUnauthServer(ctx, ex);
        }
    }
    public void catchBundleUnauthServer(Context ctx, DataAccessException ex){
        if ((ex.getExc() == DataAccessException.PosExc.Unauthorized)) {//401
            errorMessage(401, ctx, ex);
        }
        if ((ex.getExc() == DataAccessException.PosExc.ServerError)){//500
            errorMessage(500, ctx, ex);
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
            userService.authenticatUser(authToken);//Authenticate Authtoken
            GameData gameData = gameService.creategame(gameName);
            ctx.result(serializer.toJson(gameData));
            ctx.status(200).result();
        }
        catch (DataAccessException ex){
            caseBundleBadUnauthServer(ctx, ex);
        }
    }
    public void caseBundleBadUnauthServer(Context ctx, DataAccessException ex){
        if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){//400
            errorMessage(400, ctx, ex);
        }
        catchBundleUnauthServer(ctx, ex);
    }
    private void listGame(Context ctx){
        try {
            Gson serializer = new Gson();
            String authToken = ctx.header("authorization");
            userService.authenticatUser(authToken); //Authenticate Authtoken
            HashMap<Integer, GameData> games = gameService.listgames();
            Collection<GameData> gameList = games.values();
            String json = serializer.toJson(new GameList(gameList));
            ctx.result(json);
            ctx.status(200).result();
        }
        catch (DataAccessException ex){ //401
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
            userService.authenticatUser(authToken); //Authenticate Authtoken
            String username = userService.getUsernameAuth(authToken);   //get username
            GameData updatedGameData = gameService.joingame(gameID, playerColor, username); //Update
            ctx.result(serializer.toJson(updatedGameData));
            ctx.status(200).result();
        }
        catch (DataAccessException ex){
            caseBundleBadUnauthServer(ctx, ex);
            if (ex.getExc() == DataAccessException.PosExc.Forbidden) {//403
                errorMessage(403, ctx, ex);
            }
        }
        catch (Exception ex){//403
            errorMessage(403, ctx, ex);
        }
    }
    public void clearApplication(Context ctx) throws DataAccessException {
        try {
            userService.clear();
            gameService.clear();
            ctx.status(200);
        }
        catch (DataAccessException ex){
            if ((ex.getExc() == DataAccessException.PosExc.BadRequest)){//400
                errorMessage(400, ctx, ex);
            }
            if ((ex.getExc() == DataAccessException.PosExc.ServerError)){//500
                errorMessage(500, ctx, ex);
            }
        }
    }
    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }
    public void stop() {
        javalin.stop();
    }
    private void connect(UserGameCommand userGameCommandObj, WsMessageContext wsMessageContext){
        try {
            int gameID = userGameCommandObj.getGameID();
            GameData gameData = gameService.getgame(gameID);
            ChessGame game = gameData.game();
            String username = userService.getUsernameAuth(userGameCommandObj.getAuthToken());
            ServerMessageLoadGame serverMessage = new ServerMessageLoadGame(ServerMessage.ServerMessageType.LOAD_GAME, game);
            String serverSent = new Gson().toJson(serverMessage);
            wsMessageContext.send(serverSent);
            String joinColor;
            String role;
            if (gameData.whiteUsername().equals(username)){
                joinColor = "white";
                role = " joined as ";
            }
            else if (gameData.blackUsername().equals(username)){
                joinColor = "black";
                role = " joined as ";
            }
            else {
                joinColor = "";
                role = " joined as observer";
            }
            if (gameMap.get(gameID) != null) {  //game exists and has other players
                String message = username+role+joinColor;
                everyoneExceptCurPlayer(gameID, wsMessageContext, message);
            }
            else if (gameMap.get(gameID) == null){ //game doesn't exist yet
                gameMap.put(gameID, new ArrayList<>());
            }
            gameMap.get(gameID).add(wsMessageContext);
        }
        catch(Exception e){
            errorMessageSender(e, wsMessageContext);
        }
    }
    private void makeMethod(UserGameCommand userGameCommandObj, WsMessageContext wsMessageContext) throws Exception {
        int gameID = userGameCommandObj.getGameID();
        GameData gameData = gameService.getgame(gameID);
        ChessGame game = gameData.game();
        try {
            boolean hasResigned = game.getHasResigned();
            hasResigned(hasResigned);
            String username = userService.getUsernameAuth(userGameCommandObj.getAuthToken());
            MakeMoveCommand makeMoveCommand = new Gson().fromJson(wsMessageContext.message(), MakeMoveCommand.class);
            ChessMove makeMove = makeMoveCommand.getMakeMove();
            ChessPosition startPosition = makeMove.getStartPosition();
            ChessPosition endPosition = makeMove.getEndPosition();
            String startPoint = toChessNotation(startPosition);
            String endPoint = toChessNotation(endPosition);
            invalidMoveOpp(username, gameData, game);
            game.makeMove(makeMove);
            GameData gameDataUpdated = gameDao.updategame(gameData);
            List<WsMessageContext> listGamePlayers = gameMap.get(gameID);
            everyonePlayerLoadGame(listGamePlayers, game);
            String message = username+" moved from " + startPoint + " to " + endPoint;
            everyoneExceptCurPlayer(gameID, wsMessageContext, message);
            boolean checkMateStatus = checkMateChecker(game, gameDataUpdated, listGamePlayers, wsMessageContext);
            if (!checkMateStatus){
                checkChecker(game, gameDataUpdated, listGamePlayers, wsMessageContext);
                staleChecker(game, gameDataUpdated, listGamePlayers, wsMessageContext);
            }
        }
        catch (InvalidMoveException | DataAccessException e){
            errorMessageSender(e,wsMessageContext);
        }
    }
    private void leave(UserGameCommand userGameCommandObj, WsMessageContext wsMessageContext) throws Exception {
        int gameID = userGameCommandObj.getGameID();
        GameData gameData = gameService.getgame(gameID);
        String username = userService.getUsernameAuth(userGameCommandObj.getAuthToken());
        try {
            String message = "leave " + username;
            List<WsMessageContext> listGamePlayers = gameMap.get(gameID);
            everyoneExceptCurPlayer(gameID, wsMessageContext, message);
            removingAfterLeave(listGamePlayers, wsMessageContext);
            GameData gameDataUpdated = gameDao.updategameplayers(gameData, username);
        }
        catch (InvalidMoveException | DataAccessException e){
            errorMessageSender(e,wsMessageContext);
        }
    }
    private void resign(UserGameCommand userGameCommandObj, WsMessageContext wsMessageContext) throws Exception {
        int gameID = userGameCommandObj.getGameID();
        GameData gameData = gameService.getgame(userGameCommandObj.getGameID());
        ChessGame game = gameData.game();
        String username = userService.getUsernameAuth(userGameCommandObj.getAuthToken());
        try {
            if (!username.equals(gameData.whiteUsername()) && !username.equals(gameData.blackUsername())) {
                throw new InvalidMoveException("Can't resign as a observer.");
            }
            if (game.getHasResigned()) {
                throw new InvalidMoveException("Can't resign twice");
            }
            String message = "resign " + username;
            List<WsMessageContext> listGamePlayers = gameMap.get(gameID);
            everyonePlayerNotification(listGamePlayers,wsMessageContext, message);
            game.setHasResigned(true);
            GameData gameDataUpdated = gameDao.updategameresigned(gameData);
        }
        catch (InvalidMoveException e){
            errorMessageSender(e,wsMessageContext);
        }
    }
}
