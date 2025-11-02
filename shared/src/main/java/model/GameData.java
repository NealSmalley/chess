package model;

import chess.ChessGame;

public record GameData(Integer gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
    public GameData setId(int id) {
        return new GameData(id, this.whiteUsername, this.blackUsername, this.gameName, this.game);
    }
}
