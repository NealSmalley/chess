package dataaccess;

import chess.ChessGame;
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
    public GameData creategame(GameData gameData){
        int id = gameIDGenerator();
        String gameNameInput = gameData.gameName();
        GameData gameDataInput = new GameData(id, null, null,gameNameInput, null);
        games.put(id, gameDataInput);
        return gameDataInput;
    }


    public GameData joingame(GameData gamedata) throws Exception{
        //int gameID, String playerColor, String username
        int gameID = gamedata.gameID();
        String blackUsername = gamedata.blackUsername();
        String whiteUsername = gamedata.whiteUsername();

        //get game
        GameData currentGame = games.get(gameID);
        if (currentGame == null){
            throw new dataaccess.DataAccessException(DataAccessException.PosExc.BadRequest, "current game is null");
        }
        String currentGameName = currentGame.gameName();
        ChessGame currentgame = currentGame.game();
        String currentwhiteUsername = currentGame.whiteUsername();
        String currentblackUsername = currentGame.blackUsername();


        //already taken
        if (blackUsername != null){
            if (currentblackUsername != null){
                throw new Exception();
            }
            currentblackUsername = blackUsername;
        }
        if (whiteUsername != null){
            if (currentwhiteUsername != null){
                throw new Exception();
            }
            currentwhiteUsername = whiteUsername;
        }

        //update
        GameData updategamedata = new GameData(gameID, currentwhiteUsername, currentblackUsername, currentGameName, currentgame);
        games.put(gameID, updategamedata);
        return updategamedata;
    }
    public GameData updategame(GameData gamedata) throws Exception{
        return gamedata;
    }
    public GameData updategameplayers(GameData gamedata, String username) throws Exception{
        return new GameData(null, null, null, null,null);
    }
    public GameData getgame(int gameID) throws Exception{
        GameData currentGame = games.get(gameID);
        return currentGame;
    }
    public GameData updategameresigned(GameData gameData) throws Exception{
        return gameData;
    }

    public HashMap<Integer, GameData> listgames() {
        return games;
    }
}
