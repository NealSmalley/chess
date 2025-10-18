package service;

import dataaccess.MightNeed.AuthDAO;
import dataaccess.MightNeed.GameDAO;

public class GameService {
    private GameDAO gameDao;
    private AuthDAO authDao;
    public GameService(GameDAO gameDao, AuthDAO authDao){
        this.gameDao = gameDao;
        this.authDao = authDao;
    }
    public void clear() {
        gameDao.clear();
    }
}
