package dataaccess;

import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.GameData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class SQLGameDAOTest {

    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("TRUNCATE game")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        ChessGame starterChessGame = new ChessGame();
        ChessBoard chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        starterChessGame.setBoard(chessBoard);
        int gameID = 1;
        GameData starterGameData = new GameData(1, "whiteUsername", "blackUsername", "gameName", starterChessGame);
    }

    @AfterEach
    void breakDown() {
        try (var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("TRUNCATE game")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createGame() throws DataAccessException, SQLException {
        SQLGameDAO gameDao = new SQLGameDAO();
        gameDao.clear();
        ChessGame starterChessGame = new ChessGame();
        //added to db
        GameData starterGameData = new GameData(1, "whiteUsername", "blackUsername", "gameName", starterChessGame);
        gameDao.creategame(starterGameData);
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game WHERE gameID=?")) {
                    statement.setInt(1, starterGameData.gameID());
                    try (var table = statement.executeQuery()){
                        table.next();
                        ChessGame chessGame = new Gson().fromJson(table.getString("game"), ChessGame.class);
                        assertEquals(starterGameData.game(), chessGame, "no match");
                    }
                 }
        }
    }

    @Test
    void createGameInvalid() throws DataAccessException, SQLException{
        SQLGameDAO gameDao = new SQLGameDAO();
        ChessGame starterChessGame = new ChessGame();
        //added to db
        GameData starterGameData = new GameData(null, null, null, null, null);
        assertThrows(DataAccessException.class, () -> gameDao.creategame(starterGameData));
    }

    @Test
    void listGames() throws DataAccessException, SQLException {
        SQLGameDAO gameDao = new SQLGameDAO();
        gameDao.clear();
        ChessGame starterChessGame = new ChessGame();
        //added to db
        GameData starterGameData = new GameData(8, "whiteUsername", "blackUsername", "gameName2", starterChessGame);
        gameDao.creategame(starterGameData);
        HashMap<Integer,GameData> listGames = gameDao.listgames();
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT gameID, whiteUsername, blackUsername, gameName, game FROM game")) {
                try (var results = statement.executeQuery()) {
                    assertFalse(listGames.isEmpty());
                }
            }
        }
    }

    @Test
    void listGamesInvalid() throws DataAccessException {
        SQLGameDAO gameDao = new SQLGameDAO();
        ChessGame starterChessGame = new ChessGame();
        gameDao.clear();
        HashMap<Integer, GameData> gameList = gameDao.listgames();
        assertTrue(gameList.isEmpty());
    }

    @Test
    void updateGame() throws Exception {
        SQLGameDAO gameDao = new SQLGameDAO();
        gameDao.clear();
        ChessGame starterChessGame = new ChessGame();
        //added to db
        GameData starterGameData = new GameData(8, "", "", "gameName", starterChessGame);
        gameDao.creategame(starterGameData);

        GameData updateGameData = new GameData(8, "whiteUsername2", "blackUsername2", "gameName", starterChessGame);
        Assertions.assertDoesNotThrow(()->gameDao.updategame(updateGameData));
    }

    @Test
    void updateGameInvalid() throws Exception {
        SQLGameDAO gameDao = new SQLGameDAO();
        gameDao.clear();
        ChessGame starterChessGame = new ChessGame();
        GameData starterGameData = new GameData(8, "", "", "gameName", starterChessGame);
        assertThrows(DataAccessException.class, ()-> gameDao.updategame(starterGameData));
    }

    @Test
    void clear() throws DataAccessException, SQLException {
        SQLGameDAO gameDao = new SQLGameDAO();
        ChessGame starterChessGame = new ChessGame();
        GameData starterGameData = new GameData(10, "asdf", "asdf", "gameName", starterChessGame);
        gameDao.creategame(starterGameData);
        gameDao.clear();

        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT gameID FROM game WHERE gameID=?")) {
                statement.setInt(1, starterGameData.gameID());
                try (var table = statement.executeQuery()) {
                    assertFalse(table.next());
                }
            }
        }
    }




}
