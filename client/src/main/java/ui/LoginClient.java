package ui;

import ui.exception.DataAccessException;
import ui.server.ServerFacade;

import java.util.Arrays;
import java.util.Scanner;

import static ui.EscapeSequences.*;

public class LoginClient {
    private final ServerFacade server;
    private LoginStatus loginStatus = LoginStatus.SIGNEDOUT;
    private String loginName = null;
    private String gameName = null;

    public LoginClient(String serverUrl){
        server = new ServerFacade(serverUrl);
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
            if (tokens.length() > 0){
                cmd = tokens[0];
            }
            else {
                cmd = "help";
            }
            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd){
                case "register" -> register(params);
                case "login" -> login(params);
                case "quit" -> "quit";
                case "create" -> create(params);
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
            loginStatus = LoginStatus.SIGNEDIN;
            return String.format("You registered and signedin in as %s.", loginName);
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <yourname>");
    }

    public String login(String... params) throws DataAccessException {
        if (params.length >= 1){
            loginStatus = LoginStatus.SIGNEDIN;
            loginName = String.join("-", params);
            return String.format("Logged in as %s.", loginName);
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <yourname>");
    }

    public String create(String... params) throws DataAccessException {
        if ((params.length == 1) && (isLoggedIn(loginStatus))){
            gameName = String.join("-", params);
            server.create(gameName);
            return String.format("GameName in as %s.",gameName);
        }
        throw new DataAccessException(DataAccessException.PosExc.ClientError, "Expected: <gameName>");
    }
    public String list() throws DataAccessException{
        isLoggedIn(loginStatus);
        server.list();
    }

    private boolean isLoggedIn(LoginStatus loginStatus){
        if (loginStatus == LoginStatus.SIGNEDIN){
            return true;
        }
        return false;
    }


    public String help() {
        if (loginStatus == LoginStatus.SIGNEDOUT){
            return """
                   register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                   login <USERNAME> <PASSWORD> - to play chess
                   quit - playing chess
                   help - with possible commands
                   """;
        }
        else if (loginStatus == LoginStatus.SIGNEDIN){
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
}
