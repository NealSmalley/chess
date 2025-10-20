package dataaccess.MightNeed;

import model.GameData;

public interface GameDAO {
    public void clear();
    public GameData creategame(String gameName);
}
