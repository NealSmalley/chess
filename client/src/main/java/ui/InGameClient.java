//ignore this for now

//package ui;
//
//import ui.server.PrintBoard;
//
//import java.util.Arrays;
//import java.util.Scanner;
//
////websocket libraries
//import jakarta.websocket.Endpoint;
//import jakarta.websocket.Session;
//
//
//import static ui.EscapeSequences.*;
//
//public class InGameClient {
//    private String color;
//    private final WebSocketFacade ws;
//
//
//
//    public InGameClient(String serverUrl) implements NotificationHandler{
//        ws = new WebSocketFacade(serverUrl, this);
//    }
////    InGameClient(String color){
////        this.color = color;
////    }
//    public Session session;
//
//    private void printPrompt(){
//        System.out.print("\n" + SET_BG_COLOR_BLACK + ">>> " + SET_TEXT_COLOR_LIGHT_GREY);
//    }
//
//    public void run(){
//        System.out.println(SET_BG_COLOR_BLACK+SET_TEXT_COLOR_WHITE+"Here are commands for in the game");
//        System.out.print(help());
//        InGameClient client = new InGameClient();
//        Scanner scanner = new Scanner(System.in);
//        var result = "";
//        while (!result.equals("quit")){
//            printPrompt();
//            String line = scanner.nextLine();
//            try {
//                result = eval(line);
//                System.out.print(result);
//            } catch (Throwable e){
//                var msg = e.toString();
//                System.out.print(msg);
//            }
//        }
//    }
//    public String eval(String input){
//        //try {
//            String[] tokens = input.toLowerCase().split(" ");
//            String cmd;
//            if (tokens.length > 0){
//                cmd = tokens[0];
//            }
//            else {
//                cmd = "help";
//            }
//            String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
//            //what should be done about the badtype switch expressions
//            return switch (cmd){
//                case "redraw" -> redraw(color);
//                case "leave" -> leave();
//                case "make move" -> makeMove();
//                case "resign" -> resign();
//                case "highlight moves" -> highLightMoves();
//                default -> help();
//            };
//        //}
////        catch (DataAccessException ex){
////            return ex.getMessage();
////        }
//    }
//
//    private String redraw(String color){
//        PrintBoard board = new PrintBoard();
//        board.printBoard(color);
//        return "redraw is completed";
//    }
//
//    private String leave() throws Exception{
//        //put this in InGameClient
//        WebSocketFacade client = new WebSocketFacade();
//        Scanner scanner = new Scanner(System.in);
//        System.out.println("Enter a message you to echo:");
//        while(true){
//            client.send(scanner.nextLine());
//        }
//        return "leave is completed";
//    }
//    private String makeMove(){
//        return "makeMove() is completed";
//    }
//    private String resign(){
//        return "resign() is completed";
//    }
//    private String highLightMoves(){
//        return "highLightMoves() is completed";
//    }
//
//    public String help() {
//            return """
//                    help - with possible commands
//                    redraw - redraws chess board
//                    leave - removes the user from the game
//                    make move <START POINT> <END POINT> - moves piece of user
//                    resign - forfeits the game
//                    highlight moves <START POINT> - highlights the legal moves for the player
//                    """;
//    }
//
//
//}
