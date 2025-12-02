package websocket.messages;

import chess.ChessGame;

public class ServerMessageLoadGame extends ServerMessage{
    private ChessGame game;
    public ServerMessageLoadGame(ServerMessageType type, ChessGame game) {
        super(type);
        this.game = game;
    }
    public ChessGame game(){
        return game;
    }
}
