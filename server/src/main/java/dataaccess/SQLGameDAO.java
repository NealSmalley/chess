package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
//imports Connection and SQLException
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
            throw new DataAccessException(DataAccessException.PossibleExc.Sql, "SQL exception");
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
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    //gameID
                    if (param instanceof Integer p) ps.setInt(i + 1, p);
                    //whiteUsername, blackUsername, gameName
                    else if (param instanceof String p) ps.setString(i + 1, p);
                    //game
                    else if (param instanceof ChessGame p) ps.setString(i + 1, p.toString());
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
            throw new DataAccessException(DataAccessException.PossibleExc.Sql, "SQL Exception");
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
            String sql = "UPDATE game SET whiteUsername = ?, blackUsername = ?, gameName= ?, game = ? WHERE gameID = ?";
            try (var statement = conn.prepareStatement(sql)) {
                Gson gson = new Gson();
                String gameJson = gson.toJson(gamedata.game());
                statement.setString(1, gamedata.whiteUsername());
                statement.setString(2, gamedata.blackUsername());
                statement.setString(3, gamedata.gameName());
                statement.setString(4, gameJson);
                statement.setInt(5, gamedata.gameID());
                int updatedRows = statement.executeUpdate();
                //throw error if updatedrows is 0
            }
        }
        catch (SQLException e) {
            throw new DataAccessException(DataAccessException.PossibleExc.Sql, "SQL Exception");
        }
        return gamedata;
    }

    public HashMap<Integer, GameData> listgames() throws dataaccess.DataAccessException {
        //list/arraylist to preserve the order
        List<GameData> GameDatas = new ArrayList<>();
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game")) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        GameDatas.add(readGame(rs));
                    }
                }
            }
        } catch (Exception e) {
            throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.ServerError, String.format("Unable to read data: %s", e.getMessage()));
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
