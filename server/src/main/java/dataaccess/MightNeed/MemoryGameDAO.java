package dataaccess.MightNeed;

import chess.ChessGame;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.AuthData;
import model.GameData;
import server.BadRequestException;
import service.UnauthorizedException;

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
    public GameData creategame(String gameNametest){
        //Json string to string
        JsonObject obj = JsonParser.parseString(gameNametest).getAsJsonObject();
        // Extract the actual name
        String gameName = obj.get("gameName").getAsString();

        int ID = gameIDGenerator();
        GameData gamedata = new GameData(ID, null, null,gameName, null);
        games.put(ID, gamedata);
        return gamedata;
    }


    public GameData updategame(int gameID, String playerColor, String username) throws Exception{


        //get game
        GameData CurrentGame = games.get(gameID);
        if (CurrentGame == null){
            throw new BadRequestException();
        }
        String currentGameName = CurrentGame.gameName();
        ChessGame currentGame = CurrentGame.game();
        String whiteUsername = CurrentGame.whiteUsername();
        String blackUsername = CurrentGame.blackUsername();


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
        GameData updategamedata = new GameData(gameID, whiteUsername, blackUsername, currentGameName, currentGame);
        games.put(gameID, updategamedata);
        return updategamedata;
    }

    public HashMap<Integer, GameData> listgames() {
        return games;
    }
}
