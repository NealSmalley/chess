package service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;
import server.BadRequestException;

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
            throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, "gameName is empty");
        }
        //Json string to string
        JsonObject obj = JsonParser.parseString(gameName).getAsJsonObject();
        // Extract the actual name
        String gameNameInput = obj.get("gameName").getAsString();


        GameData gamedata = new GameData(null, null, null,gameNameInput, null);

        GameData gameData = gameDao.creategame(gamedata);
        return gameData;
    }

    public GameData updategame(int gameID, String playerColor, String username) throws Exception{
        GameData updatedGame = gameDao.updategame(gameID, playerColor, username);
        return updatedGame;
    }
    public HashMap<Integer, GameData> listgames(){
        HashMap<Integer, GameData> games =  gameDao.listgames();
        return games;
    }

    public void clear() {
        gameDao.clear();
    }
}
