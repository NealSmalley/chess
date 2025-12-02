package ui.server;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import java.util.List;
import java.util.Objects;
import java.util.Stack;

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
            textColor = SET_TEXT_COLOR_BLUE;
            oppositeTextColor = SET_TEXT_COLOR_RED;
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
        //boarder,white,black,white,black,white,black,white,black, boarder
        //List.of("boarder","white","black","white","black","white","black","white","black","boarder").forEach(firstRowColors::push);
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
            rowChess(rowPopStack, rowPushStack, spaceColor);
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
        ChessBoard board = game.getBoard();
        int row = (innercellCount / 8)+1;
        int col = (innercellCount % 8)+1;
        innercellCount++;
        if (col == 0){
            col++;
        }
        ChessPiece piece = board.getPiece(new ChessPosition(row,col));
        //empty spaces
        if (piece == null){
            System.out.print(EMPTY + spaceColor + " ");
        }
        else {
            ChessPiece.PieceType pieceType = piece.getPieceType();
            ChessGame.TeamColor pieceColor = piece.getTeamColor();
            if ((pieceColor == ChessGame.TeamColor.WHITE)){
                textColor = textColor;
            }
            else {
                textColor = oppositeTextColor;
            }
            String letter = switch (pieceType){
                case ChessPiece.PieceType.KING -> "K";
                case ChessPiece.PieceType.QUEEN -> "Q";
                case ChessPiece.PieceType.BISHOP -> "B";
                case ChessPiece.PieceType.KNIGHT -> "N";
                case ChessPiece.PieceType.ROOK -> "R";
                case ChessPiece.PieceType.PAWN -> "P";
            };

            System.out.print(EMPTY + spaceColor + textColor + letter);
        }
        //}
//        //lettersOuter
//        if (((innercellCount >= 1) && (innercellCount <= 8)) || ((innercellCount >= 57) && (innercellCount < 65))) {
//            String letter = lettersOutside.get(letterNumber);
//            System.out.print(EMPTY + spaceColor + textColor + letter);
//            letterNumber = letterNumber + letterIncrementer;
//        }
//        //lettersInner
//        else if(((innercellCount >=  8) && (innercellCount <= 16))||((innercellCount >= 49) && (innercellCount <= 56))) {
//            pawnCount++;
//            if (pawnCount == 9){
//                textColor = oppositeTextColor;
//            }
//            System.out.print(EMPTY + spaceColor + textColor + "P");
//            //resets letter number
//            letterNumber = resetLetterNumber;
//        }
//        //blank squares
//        else{
//            System.out.print(EMPTY + spaceColor + " ");
//        }
        row(rowPopStack, rowPushStack);
    }
}
