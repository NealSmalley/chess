package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
//imports Connection and SQLException
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;



public class SQLGameDAO implements GameDAO{
    //constructor
    public SQLGameDAO() throws DataAccessException {
        initDatabase();
    }
    //Creates Database
    private void initDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
    }

    //var with SQL table
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  game (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256),
              `blackUsername` varchar(256),
              `gameName` varchar(256) NOT NULL,
              `game` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(whiteUsername),
              INDEX(blackUsername),
              INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public void clear() throws DataAccessException {
        String sqlStatement = "TRUNCATE game";
        executeUpdateCustom(sqlStatement);
    }

    private int executeUpdateCustom(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                Gson gson = new Gson();
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    //gameID
                    if (param instanceof Integer p) ps.setInt(i + 1, p);
                    //whiteUsername, blackUsername, gameName
                    else if (param instanceof String p) ps.setString(i + 1, p);
                    //game
                    else if (param instanceof ChessGame p) ps.setString(i + 1, gson.toJson(p));
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();

                ResultSet rs = ps.getGeneratedKeys();
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
    }

    public GameData creategame(GameData gameData) throws DataAccessException {
        //gameID, whiteUsername, blackUsername, gameName, game
        var statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        int id = executeUpdateCustom(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        //in the future you might want to change this function to void
        GameData newGameData = new GameData(id, gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        return newGameData;
    }
    public GameData updategame(GameData gamedata) throws Exception{
        try (var conn = DatabaseManager.getConnection()) {
            //already taken
            String taken = "SELECT whiteUsername, blackUsername FROM game WHERE gameID = ?";
            String currentWhiteUser = null;
            String currentBlackUser = null;
            try (PreparedStatement currentStatement = conn.prepareStatement(taken)){
                currentStatement.setInt(1, gamedata.gameID());
                try (ResultSet rs = currentStatement.executeQuery()){
                    if (rs.next()){
                        currentWhiteUser = rs.getString("whiteUsername");
                        currentBlackUser = rs.getString("blackUsername");
                    } else{
                        throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, " updategame() issue with whiteUsername or blackUsername");
                    }
                }
            }
            String finalWhiteUser = "";
            String finalBlackUser = "";
            //obj whiteusername is not empty
            if (gamedata.whiteUsername() != null){
                //sql whiteusername is not empty
                if (currentWhiteUser != null && !currentWhiteUser.isEmpty()){
                    throw new DataAccessException(DataAccessException.PossibleExc.Forbidden, "403: Join Steal Team Color Exception");
                }
            }
            if (gamedata.blackUsername() != null){
                if (currentBlackUser != null && !currentBlackUser.isEmpty()){
                    throw new DataAccessException(DataAccessException.PossibleExc.Forbidden, "403: Join Steal Team Color Exception");
                }
            }

            //if whiteusername is null
            if (gamedata.whiteUsername() == null){
                finalWhiteUser = currentWhiteUser;
            }
            //not null
            else {
                finalWhiteUser = gamedata.whiteUsername();
            }
            //if blackusername is null
            if (gamedata.blackUsername() == null){
                finalBlackUser = currentBlackUser;
            }
            //not null
            else {
                finalBlackUser = gamedata.blackUsername();
            }

            //sql versions of gameName and game
            String gameName = getGameInfo(conn,gamedata,"gameName");
            String gameJson = getGameInfo(conn,gamedata,"game");


            //Update
            String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName= ?, game = ? WHERE gameID = ?";
            try (var statement = conn.prepareStatement(sql)) {
                //Gson gson = new Gson();
                //String gameJson = gson.toJson(gamedata.game());
                //String gameJsonSerialized = gson.toJson(gameJson);
                statement.setString(1, finalWhiteUser);
                statement.setString(2, finalBlackUser);
                statement.setString(3, gameName);
                statement.setString(4, gameJson);
                statement.setInt(5, gamedata.gameID());
                int updatedRows = statement.executeUpdate();
                //throw error if updatedrows is 0
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
        return gamedata;
    }
    public String getGameInfo(Connection conn, GameData gamedata, String columnLabel) throws DataAccessException, SQLException{
        String gameInfo;
        //String gameJson;
        String assigning = "SELECT gameName, game FROM game WHERE gameID = ?";
        try (PreparedStatement currentStatement = conn.prepareStatement(assigning)){
            currentStatement.setInt(1, gamedata.gameID());
            try (ResultSet rs = currentStatement.executeQuery()){
                if (rs.next()){
                    gameInfo = rs.getString(columnLabel);
                    //gameName = rs.getString("gameName");
                    //gameJson = rs.getString("game");
                } else{
                    throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, "gameName or game doesn't exist");
                }
            }
        }
        return gameInfo;
    }


    public HashMap<Integer, GameData> listgames() throws dataaccess.DataAccessException {
        //list/arraylist to preserve the order
        List<GameData> gameDatas = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        gameDatas.add(readGame(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
        HashMap<Integer, GameData> map = zipListToMap(gameDatas);
        return map;
    }


    //this will only work if the IDs are incremented 1,2,3,4...
    public HashMap<Integer, GameData> zipListToMap(List<GameData> definitions){
        HashMap<Integer, GameData> map = new HashMap<>();
        for (int i = 0; i < definitions.size(); i++) {
            map.put(i+1, definitions.get(i));
        }
        return map;
    }

    private GameData readGame(ResultSet rs) throws SQLException {
        //gameID, whiteUsername, blackUsername, gameName, game
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        String game = rs.getString("game");
        ChessGame gameData = new Gson().fromJson(game, ChessGame.class);
        GameData gameDataDeserialized = new GameData(gameID, whiteUsername, blackUsername, gameName, gameData);
        return gameDataDeserialized;
    }



}
