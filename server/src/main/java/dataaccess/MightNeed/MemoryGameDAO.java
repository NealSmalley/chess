package dataaccess.MightNeed;

import model.AuthData;
import model.GameData;

import java.util.HashMap;

public class MemoryGameDAO implements GameDAO{
    private final HashMap<Integer, GameData> games = new HashMap<>();
    @Override
    public void clear(){
        games.clear();
    }
    //creates incremental IDs
    public int gameIDGenerator(){
        int nextID = games.size() + 1;
        return nextID;
    }
    public GameData creategame(String gameName){
        int ID = gameIDGenerator();
        GameData gamedata = new GameData(ID, null, null,gameName, null);
        games.put(ID, gamedata);
        return gamedata;
    }
}
