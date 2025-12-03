package ui;

import model.AuthData;
import model.client.LoginData;
import model.GameData;
import model.GameList;
import model.UserData;
import ui.exception.ClientException;
import ui.server.PrintBoard;
import ui.server.ServerFacade;

import websocket.commands.UserGameCommand;

import java.util.*;

import static ui.EscapeSequences.*;

public class LoginClient {
    private final ServerFacade serverFacade;
    private LoginStatus loginStatus = LoginStatus.SIGNEDOUT;
    private JoinStatus joinStatus = JoinStatus.NOTJOINED;
    private String userName = null;
    private String gameName = null;
    private String authToken;
    private int gameNumber;
    private int gameListLen;
    private Map<Integer, Integer> gameNumberMap = new HashMap<>();
    public static String color;
    public Integer gameID;

    private final WebSocketFacade ws;

    public LoginClient(String serverUrl) throws Exception{
        serverFacade = new ServerFacade(serverUrl);
        ws = new WebSocketFacade(serverUrl, this);
    }
    public void run(){
        System.out.println("Welcome to the Chessgame! Sign in to start.");
        System.out.print(help());

        Scanner scanner = new Scanner(System.in);
        var result = "";
        while (!result.equals("quit")){
            printPrompt();
            String line = scanner.nextLine();

            try {
                result = eval(line);
                System.out.print(result);
            } catch (Throwable e){
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    private void printPrompt(){
        System.out.print("\n" + SET_BG_COLOR_BLACK + ">>> " + SET_TEXT_COLOR_WHITE);
    }

    public String eval(String input) {
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd;
            if (tokens.length > 0) {
                cmd = tokens[0];
            } else {
                cmd = "help";
            }
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            if (loginStatus == LoginStatus.SIGNEDOUT) {
                return signedOutCommands(cmd,params);
            }
            else if (loginStatus == LoginStatus.SIGNEDIN && joinStatus == JoinStatus.NOTJOINED){
                return signedInCommands(cmd, params);
            }
            else if (loginStatus == LoginStatus.SIGNEDIN && joinStatus == JoinStatus.JOINED){
                return joinedCommands(cmd, params);
            }
            return help();

        }catch (ClientException ex){
            return ex.getMessage();
        }
    }
    public String signedOutCommands(String cmd, String[] params) throws ClientException{
        return switch (cmd){
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> "quit";
            default -> help();
        };
    }
    public String signedInCommands(String cmd, String[] params) throws ClientException{
        return switch (cmd){
            case "create" -> createGame(params);
            case "list" -> list();
            case "join" -> join(params);
            case "observe" -> observe(params);
            case "logout" -> logout();
            case "quit" -> "quit";
            default -> help();
        };
    }
    public String joinedCommands(String cmd, String[] params) throws ClientException{
        return switch (cmd){
            case "redraw" -> redraw();
//            case "leave" -> leave();
//            case "make move" -> makeMove();
//            case "resign" -> resign();
//            case "highlight legal moves" -> legalMoves();
            default -> help();
        };
    }

    public String register(String... params) throws ClientException {
        if (params.length == 3){
            UserData userdata = toUserData(params);
            AuthData authdata = serverFacade.register(userdata);
            authToken = authdata.authToken();
            userName = authdata.username();
            return String.format("You registered as %s.", userName);
        }
        throw new ClientException("Expected: <yourname password and email>");
    }
    private UserData toUserData(String... params){
        String email;
        loginStatus = LoginStatus.SIGNEDIN;
        String username = params[0];
        String password = params[1];
        if (params.length > 2) {
            email = params[2];
        }
        else{
            email = null;
        }
        return new UserData(username, password, email);
    }
    private LoginData toLoginData(String... params){
        loginStatus = LoginStatus.SIGNEDIN;
        String username = params[0];
        String password = params[1];
        return new LoginData(username, password);
    }

    public String login(String... params) throws ClientException {
        if (params.length == 2){
            loginStatus = LoginStatus.SIGNEDIN;
            LoginData logindata = toLoginData(params);
            AuthData authdata = serverFacade.login(logindata);
            authToken = authdata.authToken();
            userName = String.join("-", params);
            return String.format("Logged in as %s.", userName);
        }
        throw new ClientException("Expected: <yourname and password>");
    }

    public String createGame(String... params) throws ClientException {
        if ((params.length == 1) && (isLoggedIn(loginStatus))){
            gameName = String.join("-", params);
            GameData newGame = serverFacade.createGame(gameName);
            int gameid = newGame.gameID();
            //gameNumber++;
            //gameNumberMap.put(gameNumber, gameid);
            return String.format("GameName in as %s.",gameName);
        }
        throw new ClientException("Expected: <gameName>");
    }
    public String list() throws ClientException {
        if (isLoggedIn(loginStatus)) {
            GameList gameList = serverFacade.listGame();
            var result = new StringBuilder();
            gameListLen = gameList.games().size();
            gameNumber = 0;
            for (GameData game : gameList.games()){
                gameNumber++;
                int gameid = game.gameID();
                String whiteUsername = game.whiteUsername();
                String blackUsername = game.blackUsername();
                String gameName = game.gameName();
                String gameNumberString = String.valueOf(gameNumber);
                result.append("gameNumber: ").append(gameNumberString).append(", ");
                result.append("whiteUsername: ").append(whiteUsername).append(", ");
                result.append("blackUsername: ").append(blackUsername).append(", ");
                result.append("gameName: ").append(gameName).append("\n");
                gameNumberMap.put(gameNumber, gameid);
            }
            return result.toString();
        }
        throw new ClientException("Expected: <loggedin>");
    }
    public String join(String... params) throws ClientException {
        int gamenumber;
        if (params.length == 2 && (isLoggedIn(loginStatus))){
            try {
                gamenumber = Integer.parseInt(params[0]);
            }
            catch (NumberFormatException e){
                throw new ClientException("Expected: <use numbers not words>",e);
            }
            if (inGameList(gamenumber)){
                String color = params[1];
                this.color = color;
                serverFacade.join(gamenumber, color, gameNumberMap);
                joinStatus = JoinStatus.JOINED;
                int gameID = gameNumberMap.get(gamenumber);
                this.gameID = gameID;
                ws.send(UserGameCommand.CommandType.CONNECT, authToken, gameID);
                return "";
            }
            else{
                throw new ClientException("Expected: <gameNumber doesn't exist>");
            }
        }
        throw new ClientException("Expected: <gameNumber and color>");
    }
    private boolean inGameList(int gamenumber){
        return (gamenumber > 0) && (gamenumber <= gameListLen);
    }
    public String observe(String... params) throws ClientException {
        if (params.length == 1 && (isLoggedIn(loginStatus))){
            int gamenumber;
            try {
                gamenumber = Integer.parseInt(params[0]);
            }
            catch (NumberFormatException e){
                throw new ClientException("Expected: <use numbers not words>", e);
            }
            if (inGameList(gamenumber)) {
//                PrintBoard board = new PrintBoard();
//                board.printBoard("white");
                int gameID = gameNumberMap.get(gamenumber);
                this.gameID = gameID;
                ws.send(UserGameCommand.CommandType.CONNECT, authToken, gameID);
                return "";
            }
            else{
                throw new ClientException("Expected: <gameNumber doesn't exist>");
            }
        }
        throw new ClientException("Expected: <gameNumber>");
    }
    public String logout() throws ClientException {
        loginStatus = LoginStatus.SIGNEDOUT;
        serverFacade.logout();
        return "logout success";
    }
    private boolean isLoggedIn(LoginStatus loginStatus){
        if (loginStatus == LoginStatus.SIGNEDIN){
            return true;
        }
        return false;
    }
    public String help() {
        if ((loginStatus == LoginStatus.SIGNEDIN) && (joinStatus == JoinStatus.NOTJOINED)) {
            return """
                    create <NAME> - a game
                    list - games
                    join <ID> [WHITE|BLACK] - a game
                    observe <ID> - a game
                    logout - when you are done
                    quit - playing chess
                    help - with possible commands
                    """;
        }
        else if ((loginStatus == LoginStatus.SIGNEDIN) && (joinStatus == JoinStatus.JOINED)) {
            return """
                    redraw - Redraws the chess board upon the user's request
                    leave - Removes the user from the game
                    make move - allows the user to input what move they want to make
                    resign - The user forfeits the game and the game is over
                    highlight legal moves - The selected piece's current square and all squares it can legally move to are highlighted
                    help - Displays text information the user what actions they can take
                    """;
        }
        else {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }

    }
    public GameData currentGame() throws ClientException{
        GameList gameList = serverFacade.listGame();
        for (GameData game : gameList.games()){
            if (Objects.equals(gameID, game.gameID())){
                return game;
            }
        }
        throw new ClientException("Game ID: "+ gameID+" wasn't found");
    }


    public String redraw() throws ClientException{
        //ws.send(UserGameCommand.CommandType.CONNECT, authToken, gameID);
        GameList gameList = serverFacade.listGame();
        for (GameData game : gameList.games()){
            if (Objects.equals(gameID, game.gameID())){
                PrintBoard board = new PrintBoard();
                board.printBoard(game.game(),color);
            }
        }
        return "";
    }

//    public String leave() throws ClientException{
//        GameData game = currentGame();
//    }


}
