package dataaccess;

import chess.ChessGame;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.GameData;
import server.BadRequestException;

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


    public GameData updategame(int gameID, String playerColor, String username) throws Exception{


        //get game
        GameData currentGame = games.get(gameID);
        if (currentGame == null){
            throw new BadRequestException();
        }
        String currentGameName = currentGame.gameName();
        ChessGame currentgame = currentGame.game();
        String whiteUsername = currentGame.whiteUsername();
        String blackUsername = currentGame.blackUsername();


        //already taken
        if (playerColor.equals("BLACK")){
            if (blackUsername != null){
                throw new Exception();
            }
            blackUsername = username;
        }
        if (playerColor.equals("WHITE")){
            if (whiteUsername != null){
                throw new Exception();
            }
            whiteUsername = username;
        }


        //update
        GameData updategamedata = new GameData(gameID, whiteUsername, blackUsername, currentGameName, currentgame);
        games.put(gameID, updategamedata);
        return updategamedata;
    }

    public HashMap<Integer, GameData> listgames() {
        return games;
    }
}
