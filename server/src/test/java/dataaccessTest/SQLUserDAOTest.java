package dataaccessTest;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import dataaccess.SQLAuthDAO;
import dataaccess.SQLUserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

public class SQLUserDAOTest {
    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("TRUNCATE user")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        UserData starterUser = new UserData("username", "password", "email");
    }

    @AfterEach
    void breakDown() {
        try (var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("TRUNCATE user")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createUser() throws DataAccessException, SQLException {
        SQLUserDAO sqlUserDao = new SQLUserDAO();
        UserData starterUser = new UserData("username", "password", "email");
        sqlUserDao.createUser(starterUser);
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")) {
                statement.setString(1, starterUser.username());
                try (var table = statement.executeQuery()){
                    table.next();
                    String tableUsername = table.getString("username");
                    assertEquals(tableUsername, starterUser.username());
                }
            }
        }
    }

    @Test
    void createUserInvalid() throws DataAccessException, SQLException {
        SQLUserDAO sqlUserDao = new SQLUserDAO();
        UserData starterUser = new UserData(null, null, null);
        assertThrows(DataAccessException.class, () -> sqlUserDao.createUser(starterUser));
    }

    @Test
    void getUser() throws DataAccessException {
        SQLUserDAO sqlUserDao = new SQLUserDAO();
        UserData starterUser = new UserData("username", "password", "email");
        sqlUserDao.createUser(starterUser);
        UserData retrievedUser = sqlUserDao.getUser(starterUser.username());
        assertEquals(retrievedUser.username(), starterUser.username());
    }

    @Test
    void getUserInvalid() throws DataAccessException {
        SQLUserDAO sqlUserDao = new SQLUserDAO();
        sqlUserDao.clear();
        UserData starterUser = sqlUserDao.getUser("fakeusername");
        assertNull(starterUser);
    }

    @Test
    void clear() throws DataAccessException, SQLException {
        SQLUserDAO sqlUserDao = new SQLUserDAO();
        UserData starterUser = new UserData("username", "password", "email");
        sqlUserDao.createUser(starterUser);
        sqlUserDao.clear();

        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("SELECT username, password, email FROM user WHERE username=?")){
                statement.setString(1, starterUser.username());
                try (var table = statement.executeQuery()){
                    assertFalse(table.next());
                }
            }
        }

    }





}
