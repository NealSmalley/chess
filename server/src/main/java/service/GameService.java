package service;

import dataaccess.MightNeed.AuthDAO;
import dataaccess.MightNeed.GameDAO;
import model.GameData;

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

    public void clear() {
        gameDao.clear();
    }
}
