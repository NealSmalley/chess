package dataaccess;

import com.google.gson.Gson;
import model.*;
import service.UnauthorizedException;
//imports Connection and SQLException
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;



public class SQLGameDAO {
    //constructor
    public SQLGameDAO() throws DataAccessException{
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
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }

    //var with SQL table
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  game (
              `gameID` int NOT NULL AUTO_INCREMENT,
              `whiteUsername` varchar(256) NOT NULL,
              `blackUsername` varchar(256) NOT NULL,
              `gameName` varchar(256) NOT NULL,
              `json` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(whiteUsername),
              INDEX(blackUsername),
              INDEX(gameName)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public void clear() throws DataAccessException{
        String sqlStatement = "TRUNCATE game";
        executeUpdate(sqlStatement);
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    //gameID
                    if (param instanceof Integer p) ps.setInt(i + 1, p);
                    //whiteUsername, blackUsername, gameName
                    else if (param instanceof String p) ps.setString(i + 1, p);
                    //game
                    else if (param instanceof GameData p) ps.setString(i + 1, p.toString());
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
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    public GameData creategame(GameData gameData) throws DataAccessException{
        //gameID, whiteUsername, blackUsername, gameName, game
        var statement = "INSERT INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        int id = executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        //in the future you might want to change this function to void
        return gameData;
    }
    //IMPORTANT!!!! change updategame() to take only a gameData instead of other things
    public GameData updategame(GameData gameData) throws Exception{
        var statement = "UPDATE INTO game (gameID, whiteUsername, blackUsername, gameName, game) VALUES (?, ?, ?, ?, ?)";
        int id = executeUpdate(statement, gameData.gameID(), gameData.whiteUsername(), gameData.blackUsername(), gameData.gameName(), gameData.game());
        return gameData;
    }
    public HashMap<Integer, GameData> listgames() throws DataAccessException{
        //list/arraylist to preserve the order
        List<GameData> GameDatas = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName FROM game";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GameDatas.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, String.format("Unable to read data: %s", e.getMessage()));
        }
        HashMap<Integer, GameData> map = zipListToMap(GameDatas);
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
        var gameID = rs.getInt("id");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        GameData gameData = new Gson().fromJson("json", GameData.class);
        return gameData.setId(gameID);
    }



}
