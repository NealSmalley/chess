package service;

import dataaccess.MightNeed.AuthDAO;
import dataaccess.MightNeed.GameDAO;
import model.GameData;

import java.util.HashMap;

public class GameService {
    private GameDAO gameDao;
    private AuthDAO authDao;
    public GameService(GameDAO gameDao, AuthDAO authDao){
        this.gameDao = gameDao;
        this.authDao = authDao;
    }

    public GameData creategame(String gameName){
        GameData gameData = gameDao.creategame(gameName);
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
