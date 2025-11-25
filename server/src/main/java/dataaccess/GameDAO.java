package dataaccess;

import model.GameData;

import java.util.HashMap;

public interface GameDAO {
    public void clear() throws DataAccessException;
    public GameData creategame(GameData gameName) throws DataAccessException;
    public GameData joingame(GameData gameDataUpdate) throws Exception;
    public GameData updategame(GameData gameDataUpdate) throws Exception;
    public HashMap<Integer, GameData> listgames() throws dataaccess.DataAccessException;
    public GameData getgame(int gameID) throws Exception;
    public GameData updategameplayers(GameData gamedata, String username) throws Exception;
    public GameData updategameresigned(GameData gameData) throws Exception;
}
