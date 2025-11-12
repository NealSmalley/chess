package ui.server;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class PrintBoard {
    Stack<String> colStack = new Stack<>();
    //boarder, 1st,2nd,1st,2nd,1st,2nd,1st,2nd,1st,2nd,boarder
    Stack<String> firstRowColors = new Stack<>();
    //boarder,white,black,white,black,white,black,white,black, boarder
    Stack<String> firstRowLetters = new Stack<>();
    //
    Stack<String> secondRowColors = new Stack<>();
    public enum TopBoarder{
        a,
        b,
        c,
        d,
        e,
        f,
        g,
        h;
    }
    List<String> letters = List.of("R","N","B","Q","K","B","N","R");

    //color
    private String playerColor;
    private int start;
    private int end;
    private int incrementer;
    private int sideNumber;
    private int sideIncrementer;
    private int letterNumber;
    private int letterIncrementer;

    //effected by color
    if (white){
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

    public void printBoard(String playerColor){
        switch(playerColor){
            case "white" ->
            case "black" ->
        }
        String currentCol = colStack.pop();
        switch(currentCol) {
            case "boarder" -> boarder();
            case "1st" -> firstrow();
            case "2nd" -> secondrow();
        }

        //incrementer (if called twice)
        //fill first stack()
        //pop off the first stack
        //push to the second stack
        //black
            //letter row()
    }

    public void boarder(){
        //forward = white/reverse = black
        for (int i = start; i != end; i = i+incrementer){
            System.out.println(TopBoarder.values()[i]);
        }
    }
    public void firstrow(){
        String square = firstRowColors.pop();
        System.out.println(square);
        //if square == boarder
            sideBoarder();
        //if square == white || black
            //firstRowChess()
        secondRowColors.push(square);
    }
    public void sideBoarder(){
        System.out.println(sideNumber);
        sideNumber = sideNumber + sideIncrementer;
    }
    public void firstRowChess(){
        String square = firstRowColors.pop();
        //color squares
        System.out.println(square);
        secondRowColors.push(square);
        //letters
        String letter = letters.get(letterNumber);
        System.out.println(letter);
        letterNumber = letterNumber + letterIncrementer;

    }

}
