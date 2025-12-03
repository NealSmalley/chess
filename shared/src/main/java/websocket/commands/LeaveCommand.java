package websocket.commands;

import chess.ChessMove;

public class LeaveCommand extends UserGameCommand {
    private String playerColor;

    public LeaveCommand(CommandType commandType, String authToken, Integer gameID, String playerColor) {
        super(commandType, authToken, gameID);
        this.playerColor = playerColor;
    }
    public String getPlayerColor(){
        return playerColor;
    }
}
