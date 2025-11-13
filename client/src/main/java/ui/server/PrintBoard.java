package ui.server;

import java.util.List;
import java.util.Objects;
import java.util.Stack;

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

    //color based vars
    private String playerColor;
    private int start;
    private int end;
    private int incrementer;
    private int sideNumber;
    private int sideIncrementer;
    private int letterNumber;
    private int letterIncrementer;

    //row based vars
    private Stack<String> rowPopStack;
    private Stack<String> rowPushStack;
    private int rowCount;

    public void playerColorVars(String playerColor){
        //effected by color
        if (Objects.equals(playerColor, "white")){
            start = 0;
            end = TopBoarder.values().length;
            incrementer = 1;
            sideNumber = 8;
            sideIncrementer = -1;
            letterNumber = 0;
            letterIncrementer = 1;
        }
        else {
            start = TopBoarder.values().length - 1;
            end = -1;
            incrementer = -1;
            sideNumber = 0;
            sideIncrementer = 1;
            letterNumber = 8;
            letterIncrementer = -1;
        }
    }
    public void printBoard(String playerColor){
        playerColorVars(playerColor);
        String currentCol = colStack.pop();
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
    }
    public void boarder(){
        //forward = white/reverse = black
        for (int i = start; i != end; i = i+incrementer){
            System.out.println(TopBoarder.values()[i]);
        }
    }
    public void row(Stack<String> rowPopStack,Stack<String> rowPushStack){
        rowCount++;
        //side boards
        String square = rowPopStack.pop();
        rowPushStack.push(square);
        System.out.println(square);

        perimeterOrArea(rowPopStack,rowPushStack,square);
    }
    public void sideBoarder(){
        System.out.println(sideNumber);
        sideNumber = sideNumber + sideIncrementer;
    }
    public void RowChess(Stack<String> rowPopStack, Stack<String> rowPushStack){
        String square = rowPopStack.pop();
        //color squares
        System.out.println(square);
        rowPushStack.push(square);
        //letters
        letters();
    }
    private void perimeterOrArea(Stack<String> rowPopStack, Stack<String> rowPushStack, String square){
        //side boarder
        if (Objects.equals(square, "boarder")) {
            sideBoarder();
        }
        //inner area row
        if ((Objects.equals(square, "white")) || (Objects.equals(square, "black"))) {
            RowChess(rowPopStack, rowPushStack);
        }
    }

    private void letters(){
        //lettersOuter
        if ((rowCount == 2)||(rowCount == 9)) {
            String letter = lettersOutside.get(letterNumber);
            System.out.println(letter);
        }
        //lettersInner
        else if((rowCount == 3)||(rowCount == 8)) {
            System.out.println("P");
        }
        letterNumber = letterNumber + letterIncrementer;
    }

}
