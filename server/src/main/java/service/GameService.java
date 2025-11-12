package service;

import chess.ChessGame;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import model.GameData;
import dataaccess.DataAccessException;

import java.util.HashMap;

public class GameService {
    private GameDAO gameDao;
    private AuthDAO authDao;
    public GameService(GameDAO gameDao, AuthDAO authDao){
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    public GameData creategame(String gameName) throws DataAccessException {
        //bad request
        if (gameName.equals("")){
            throw new dataaccess.DataAccessException(DataAccessException.PosExc.BadRequest, "gameName empty");
        }
        //Json string to string
        JsonObject obj = JsonParser.parseString(gameName).getAsJsonObject();
        // Extract the actual name
        String gameNameInput = obj.get("gameName").getAsString();

        ChessGame game = new ChessGame();
        GameData gamedata = new GameData(0, null, null,gameNameInput, game);

        GameData gameData = gameDao.creategame(gamedata);
        return gameData;
    }

    public GameData updategame(int gameID, String playerColor, String username) throws Exception{
        String blackUsername = null;
        String whiteUsername = null;

        if (playerColor.equals("BLACK")){
            blackUsername = username;
        }
        if (playerColor.equals("WHITE")){
            whiteUsername = username;
        }
        ChessGame game = new ChessGame();
        GameData gameDataUpdate = new GameData(gameID, whiteUsername, blackUsername, "", game);

        GameData updatedGame = gameDao.updategame(gameDataUpdate);
        return updatedGame;
    }
    public HashMap<Integer, GameData> listgames() throws dataaccess.DataAccessException {
        HashMap<Integer, GameData> games =  gameDao.listgames();
        return games;
    }

    public void clear() throws DataAccessException {
        gameDao.clear();
    }
}
