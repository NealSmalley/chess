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
}
