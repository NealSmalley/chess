package ui;

import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.GameList;
import model.UserData;
import ui.exception.DataAccessException;
import ui.server.ServerFacade;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class LoginClient {
    private final ServerFacade serverFacade;
    private LoginStatus loginStatus = LoginStatus.SIGNEDOUT;
    private String userName = null;
    private String gameName = null;
    private String authToken;
    private int gameNumber;
    private int gameListLen;
    private Map<Integer, Integer> gameNumberMap;

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
                System.out.print(SET_BG_COLOR_BLUE + result);
            } catch (Throwable e){
                var msg = e.toString();
                System.out.print(msg);
            }
        }
    }

    private void printPrompt(){
        System.out.print("\n" + SET_BG_COLOR_BLACK + ">>> " + SET_TEXT_COLOR_GREEN);
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
                case "observe" -> observe();
                case "logout" -> logout();
                default -> help();
            };
            } catch (DataAccessException ex){
                return ex.getMessage();
        }
    }

    public String register(String... params) throws DataAccessException{
        if (params.length == 4){
            UserData userdata = toUserData(params);
            AuthData authdata = serverFacade.register(userdata);
            authToken = authdata.authToken();
            return String.format("You registered as %s.", userName);
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <yourname>");
    }
    private UserData toUserData(String... params){
        String email;
        loginStatus = LoginStatus.SIGNEDIN;
        String username = params[1];
        String password = params[2];
        if (params[3] != null) {
            email = params[3];
        }
        else{
            email = null;
        }
        return new UserData(username, password, email);
    }

    public String login(String... params) throws DataAccessException {
        if (params.length >= 1){
            loginStatus = LoginStatus.SIGNEDIN;
            UserData userdata = toUserData(params);
            AuthData authdata = serverFacade.login(userdata);
            authToken = authdata.authToken();
            userName = String.join("-", params);
            return String.format("Logged in as %s.", userName);
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <yourname>");
    }

    public String createGame(String... params) throws DataAccessException {
        if ((params.length == 1) && (isLoggedIn(loginStatus))){
            gameName = String.join("-", params);
            serverFacade.createGame(gameName);
            return String.format("GameName in as %s.",gameName);
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <gameName>");
    }
    public String list() throws DataAccessException {
        if (isLoggedIn(loginStatus)) {
            GameList gameList = serverFacade.listGame();
            var result = new StringBuilder();
            gameListLen = gameList.games().size();
            for (GameData game : gameList.games()){
                gameNumber++;
                int gameid = game.gameID();
                String whiteUsername = game.whiteUsername();
                String blackUsername = game.blackUsername();
                String gameName = game.gameName();
                String gameNumberString = String.valueOf(gameNumber);
                result.append(gameNumberString).append(whiteUsername).append(blackUsername).append(gameName).append("\n");
                gameNumberMap.put(gameNumber, gameid);
            }
            return result.toString();
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <loggedin>");
    }

    public String join(String... params) throws DataAccessException{
        if (params.length == 3 && (isLoggedIn(loginStatus))){
            int gamenumber = Integer.parseInt(params[1]);
            if (inGameList(gamenumber)){
                String color = params[2];
                serverFacade.join(gamenumber, color, gameNumberMap);
                return gamenumber + color;
            }
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <join>");
    }
    private boolean inGameList(int gamenumber){
        return (gamenumber > 0) || (gamenumber <= gameListLen);
    }

    public String observe(String... params) throws DataAccessException {
        if (params.length == 2 && (isLoggedIn(loginStatus))){
            int gamenumber = Integer.parseInt(params[1]);
            if (inGameList(gamenumber)) {
                return "Valid GameNumber";
            }
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <join>");
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
