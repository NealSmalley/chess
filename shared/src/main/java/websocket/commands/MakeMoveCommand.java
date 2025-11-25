package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand{
    private ChessMove move;
    public MakeMoveCommand(CommandType commandType, String authToken, Integer gameID, ChessMove makeMove){
        super(commandType, authToken, gameID);
        this.move = makeMove;
    }
    public ChessMove getMakeMove(){
        return move;
    }
}
