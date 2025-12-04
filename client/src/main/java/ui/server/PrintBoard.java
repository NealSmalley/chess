package ui.server;

import chess.*;

import java.util.*;

import static ui.EscapeSequences.*;

public class PrintBoard {
    Stack<String> colStack = new Stack<>();
    //boarder, 1st,2nd,1st,2nd,1st,2nd,1st,2nd,1st,2nd,boarder
    Stack<String> firstRowColors = new Stack<>();
    //boarder,white,black,white,black,white,black,white,black, boarder
    Stack<String> firstRowLetters = new Stack<>();
    //
    Stack<String> secondRowColors = new Stack<>();
    public enum TopBoarder{a, b, c, d, e, f, g, h;}
    List<String> lettersOutside = List.of("R","N","B","Q","K","B","N","R");

    //universal vars
    private int sideBoarderCount;
    private int pawnCount;
    private ChessGame game;
    private boolean highlight = false;
    private Collection<ChessMove> validMoves;

    //color based vars
    private String playerColor;
    private String textColor;
    private String oppositeTextColor;
    private int start;
    private int end;
    private int incrementer;
    private int sideNumber;
    private int sideIncrementer;
    private int letterNumber;
    private int letterIncrementer;
    private int resetLetterNumber;

    //row based vars
    private Stack<String> rowPopStack;
    private Stack<String> rowPushStack;
    private Stack<String> switchStack;
    private int innercellCount;

    public void playerColorVars(String playerColor){
        //effected by color
        if (Objects.equals(playerColor, "white")){
            start = 0;
            end = TopBoarder.values().length;
            incrementer = 1;
            sideNumber = 8;
            sideIncrementer = -1;
            letterNumber = 0;
            resetLetterNumber = 0;
            letterIncrementer = 1;
            textColor = SET_TEXT_COLOR_RED;
            oppositeTextColor = SET_TEXT_COLOR_BLUE;
            List.of("boarder","black","white","black","white","black","white","black","white","boarder").forEach(firstRowColors::push);
        }
        else {
            start = TopBoarder.values().length - 1;
            end = -1;
            incrementer = -1;
            sideNumber = 1;
            sideIncrementer = 1;
            letterNumber = 7;
            resetLetterNumber = 7;
            letterIncrementer = -1;
            textColor = SET_TEXT_COLOR_RED;
            oppositeTextColor = SET_TEXT_COLOR_BLUE;
            List.of("boarder","black","white","black","white","black","white","black","white","boarder").forEach(firstRowColors::push);
        }
    }
    public void printBoardHighlight(ChessGame game,String color, Collection<ChessMove> validMoves){
        this.validMoves = validMoves;
        highlight = true;
        printBoard(game, color);
    }
    public void printBoard(ChessGame game, String color){
        this.game = game;
        if (color.equals("white")) {
            playerColor = "white";
        }
        else{
            playerColor = "black";
        }
        playerColorVars(playerColor);
        //fill colStack
        List.of("boarder","2nd","1st","2nd","1st","2nd","1st","2nd","1st","boarder").forEach(colStack::push);
        currentColOptions();
    }
    //loops through columns
    private void currentColOptions(){
        String currentCol;
        if (!colStack.isEmpty()){
            currentCol = colStack.pop();
            if (currentCol.equals("boarder")){
                boarder();
            }
            else if (currentCol.equals("1st")){
                rowPopStack = firstRowColors;
                rowPushStack = secondRowColors;
                row(rowPopStack, rowPushStack);
            }
            else if (currentCol.equals("2nd")){
                rowPopStack = secondRowColors;
                rowPushStack = firstRowColors;
                row(rowPopStack, rowPushStack);
            }
            currentColOptions();
        }
    }

    public void boarder(){
        //Left padding
        System.out.print(EMPTY+" ");
        //forward = white/reverse = black
        for (int i = start; i != end; i = i+incrementer){
            System.out.print(SET_TEXT_COLOR_BLACK + SET_BG_COLOR_LIGHT_GREY + EMPTY + TopBoarder.values()[i]);
        }
        System.out.println();
    }
    public void row(Stack<String> rowPopStack,Stack<String> rowPushStack){

        //moves through cells
        if (rowPopStack.isEmpty()) {
            switchStack = rowPopStack;
            rowPopStack = rowPushStack;
            rowPushStack = switchStack;
            System.out.println();
        }
        String square = rowPopStack.pop();
        rowPushStack.push(square);

        //side boarder
        if (Objects.equals(square, "boarder") && (sideBoarderCount <=16)) {
            sideBoarder(rowPopStack, rowPushStack);
        }
        //inner area row
        else if ((Objects.equals(square, "white")) || (Objects.equals(square, "black"))) {
            String spaceColor = spaceColor(square);
            if (highlight == false) {
                rowChess(rowPopStack, rowPushStack, spaceColor);
            }
            else if (highlight == true) {
                rowChessHighlight(rowPopStack, rowPushStack, spaceColor, validMoves);
            }
        }
    }
    private String spaceColor(String square){
        String spaceColor;
        if ((Objects.equals(square, "white"))){
            spaceColor = SET_BG_COLOR_WHITE;
        }
        else if ((Objects.equals(square, "black"))){
            spaceColor = SET_BG_COLOR_BLACK;
        }
        else{
            spaceColor = square;
        }
        return spaceColor;
    }
    public void sideBoarder(Stack<String> rowPopStack,Stack<String> rowPushStack){
        if((sideNumber != 0) && (sideNumber != 9)){
            //should increment once everytime this functions is called
            System.out.print(EMPTY + SET_BG_COLOR_LIGHT_GREY + SET_TEXT_COLOR_BLACK+ sideNumber);
            sideBoarderCount++;
            if ((sideBoarderCount % 2 == 0)) {
                sideNumber = sideNumber + sideIncrementer;
            }
            row(rowPopStack, rowPushStack);
        }
        else{
            sideBoarderCount++;
        }
    }
    public void rowChess(Stack<String> rowPopStack, Stack<String> rowPushStack, String spaceColor){
        List<Integer> coordinateList = sharedRowChess();
        int row = coordinateList.get(0);
        int col = coordinateList.get(1);
        ChessBoard board = game.getBoard();
        ChessPiece piece = board.getPiece(new ChessPosition(row,col));
        //empty spaces
        String printTextColor;
        if (piece == null){
            System.out.print(EMPTY + spaceColor + " ");
        }
        else {
            ChessPiece.PieceType pieceType = piece.getPieceType();
            ChessGame.TeamColor pieceColor = piece.getTeamColor();
            if ((pieceColor == ChessGame.TeamColor.WHITE)){
                printTextColor = textColor;
            }
            else {
                printTextColor = oppositeTextColor;
            }
            String letter = pieceToLetter(pieceType);
            System.out.print(EMPTY + spaceColor + printTextColor + letter);
        }
        row(rowPopStack, rowPushStack);
    }
    public void rowChessHighlight(Stack<String> rowPopStack, Stack<String> rowPushStack, String spaceColor, Collection<ChessMove> validMoves){
        List<Integer> coordinateList = sharedRowChess();
        int row = coordinateList.get(0);
        int col = coordinateList.get(1);
        ChessBoard board = game.getBoard();
        ChessPiece piece = board.getPiece(new ChessPosition(row,col));

        for (ChessMove move : validMoves) {
            ChessPosition startPosition = move.getStartPosition();
            ChessPosition endPosition = move.getEndPosition();
            int endRow = endPosition.getRow();
            int endCol = endPosition.getColumn();
            int startRow = startPosition.getRow();
            int startCol = startPosition.getColumn();
            if ((startRow == row) && (startCol == col)){
                spaceColor = SET_BG_COLOR_YELLOW;
            }

            if ((endRow == row) && (endCol == col)){
                spaceColor = SET_BG_COLOR_GREEN;
            }
        }

        //empty spaces
        String printTextColor;
        if (piece == null){
            System.out.print(EMPTY + spaceColor + " ");
        }
        else {
            ChessPiece.PieceType pieceType = piece.getPieceType();
            ChessGame.TeamColor pieceColor = piece.getTeamColor();
            if ((pieceColor == ChessGame.TeamColor.WHITE)){
                printTextColor = textColor;
            }
            else {
                printTextColor = oppositeTextColor;
            }

            String letter = pieceToLetter(pieceType);

            System.out.print(EMPTY + spaceColor + printTextColor + letter);
        }
        row(rowPopStack, rowPushStack);
    }

    private List<Integer> sharedRowChess(){
        List<Integer> coordinateList = new ArrayList<>();

        int row;
        int col;
        //if white start 64 decrement
        if (playerColor.equals("white")){
            row = 8-(innercellCount / 8);
            col = (innercellCount % 8) + 1;
        }
        //if black start 1 increment
        else {
            row = (innercellCount / 8) + 1;
            col = 8-(innercellCount % 8);
        }
        innercellCount++;
        coordinateList.add(row);
        coordinateList.add(col);

        return coordinateList;
    }
    private String pieceToLetter(ChessPiece.PieceType  pieceType){
        String letter = switch (pieceType){
            case ChessPiece.PieceType.KING -> "K";
            case ChessPiece.PieceType.QUEEN -> "Q";
            case ChessPiece.PieceType.BISHOP -> "B";
            case ChessPiece.PieceType.KNIGHT -> "N";
            case ChessPiece.PieceType.ROOK -> "R";
            case ChessPiece.PieceType.PAWN -> "P";
        };
        return letter;
    }
}
