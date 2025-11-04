package dataaccess;

import model.GameData;

import java.util.HashMap;

public interface GameDAO {
    public void clear() throws DataAccessException;
    public GameData creategame(GameData gameName) throws DataAccessException;
    public GameData updategame(GameData gameDataUpdate) throws Exception;
    public HashMap<Integer, GameData> listgames() throws dataaccess.DataAccessException;
}
