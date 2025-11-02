package dataaccess;

import model.GameData;

import java.util.HashMap;

public interface GameDAO {
    public void clear();
    public GameData creategame(GameData gameName);
    public GameData updategame(int gameID, String playerColor, String username) throws Exception;
    public HashMap<Integer, GameData> listgames();
}
