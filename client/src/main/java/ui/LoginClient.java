package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.Client.LoginData;
import model.GameData;
import model.GameList;
import model.UserData;
import ui.exception.DataAccessException;
import ui.server.PrintBoard;
import ui.server.ServerFacade;

import java.util.*;

import static ui.EscapeSequences.*;

public class LoginClient {
    private final ServerFacade serverFacade;
    private LoginStatus loginStatus = LoginStatus.SIGNEDOUT;
    private String userName = null;
    private String gameName = null;
    private String authToken;
    private int gameNumber;
    private int gameListLen;
    private Map<Integer, Integer> gameNumberMap = new HashMap<>();

    public LoginClient(String serverUrl){
        serverFacade = new ServerFacade(serverUrl);
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

    public String eval(String input){
        try {
            String[] tokens = input.toLowerCase().split(" ");
            String cmd;
            if (tokens.length > 0){
                cmd = tokens[0];
            }
            else {
                cmd = "help";
            }
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            //what should be done about the badtype switch expressions
            return switch (cmd){
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> "quit";
                case "create" -> createGame(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "logout" -> logout();
                default -> help();
            };
            } catch (DataAccessException ex){
                return ex.getMessage();
        }
    }

    public String register(String... params) throws DataAccessException{
        if (params.length == 3){
            UserData userdata = toUserData(params);
            AuthData authdata = serverFacade.register(userdata);
            authToken = authdata.authToken();
            userName = authdata.username();
            return String.format("You registered as %s.", userName);
        }
        throw new DataAccessException("Expected: <yourname password and email>");
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

    public String login(String... params) throws DataAccessException {
        if (params.length >= 1){
            loginStatus = LoginStatus.SIGNEDIN;
            LoginData logindata = toLoginData(params);
            AuthData authdata = serverFacade.login(logindata);
            authToken = authdata.authToken();
            userName = String.join("-", params);
            return String.format("Logged in as %s.", userName);
        }
        throw new DataAccessException("Expected: <yourname and password>");
    }

    public String createGame(String... params) throws DataAccessException {
        if ((params.length == 1) && (isLoggedIn(loginStatus))){
            gameName = String.join("-", params);
            GameData newGame = serverFacade.createGame(gameName);
            int gameid = newGame.gameID();
            //gameNumber++;
            //gameNumberMap.put(gameNumber, gameid);
            return String.format("GameName in as %s.",gameName);
        }
        throw new DataAccessException("Expected: <gameName>");
    }
    public String list() throws DataAccessException {
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
        throw new DataAccessException("Expected: <loggedin>");
    }

    public String join(String... params) throws DataAccessException{
        if (params.length == 2 && (isLoggedIn(loginStatus))){
            int gamenumber = Integer.parseInt(params[0]);
            if (inGameList(gamenumber)){
                String color = params[1];
                serverFacade.join(gamenumber, color, gameNumberMap);
                PrintBoard board = new PrintBoard();
                board.printBoard(color);
                return "";
            }
        }
        throw new DataAccessException("Expected: <gameid and color>");
    }
    private boolean inGameList(int gamenumber){
        return (gamenumber > 0) || (gamenumber <= gameListLen);
    }

    public String observe(String... params) throws DataAccessException {
        if (params.length == 1 && (isLoggedIn(loginStatus))){
            int gamenumber = Integer.parseInt(params[0]);
            if (inGameList(gamenumber)) {
                PrintBoard board = new PrintBoard();
                board.printBoard("white");
                return "";
            }
        }
        throw new DataAccessException("Expected: <gameid>");
    }
    public String logout() throws DataAccessException{
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
        if (loginStatus == LoginStatus.SIGNEDOUT) {
            return """
                    register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                    login <USERNAME> <PASSWORD> - to play chess
                    quit - playing chess
                    help - with possible commands
                    """;
        }
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
}
