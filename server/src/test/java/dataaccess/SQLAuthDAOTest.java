package dataaccess;

import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;


public class SQLAuthDAOTest {


    @BeforeEach
    void setUp() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("TRUNCATE auth")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        AuthData starterAuth = new AuthData("exaUsername", "exaToken");
    }

    @AfterEach
    void breakDown() {
        try (var conn = DatabaseManager.getConnection()){
            try(var statement = conn.prepareStatement("TRUNCATE auth")){
                statement.executeUpdate();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (DataAccessException e) {
            throw new RuntimeException(e);
        }
    }
    //DAO tests
    @Test
    void createAuth() throws DataAccessException, SQLException {
        String tableUsername;
        String tableToken;

        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        AuthData starterAuth = new AuthData("exaToken", "exaUsername");
        sqlAuthDao.createAuth(starterAuth);
        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("SELECT username, authToken FROM auth WHERE username=?")){
                statement.setString(1,starterAuth.username());
                try (var table = statement.executeQuery()){
                    table.next();
                    tableUsername = table.getString("username");
                    tableToken = table.getString("authToken");
                }
            }
        }
        assertEquals(starterAuth, new AuthData(tableToken,tableUsername));
    }

    @Test
    void createAuthInvalid() throws DataAccessException, SQLException {
        String tableUsername;
        String tableToken;
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        AuthData starterAuth = new AuthData(null, "exaUsername");
        Assertions.assertThrows(DataAccessException.class, ()->sqlAuthDao.createAuth(starterAuth));

        try (var conn = DatabaseManager.getConnection()){
            try (var statement = conn.prepareStatement("SELECT username, authToken FROM auth WHERE username=?")){
                statement.setString(1,starterAuth.username());
                try (var table = statement.executeQuery()){
                    assertFalse(table.next(), "Row should not exist after failed createAuth");
                }
            }
        }
    }

    @Test
    void removeAuth() throws DataAccessException, SQLException {
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        String authToken = "exaToken";
        AuthData starterAuth = new AuthData(authToken, "exaUsername");
        sqlAuthDao.createAuth(starterAuth);
        sqlAuthDao.removeAuth(authToken);
        databaseConnection(starterAuth);
    }

    @Test
    void removeAuthInvalid() throws DataAccessException, SQLException {
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        String authTokenError = "ErrorToken";
        Assertions.assertDoesNotThrow(()->sqlAuthDao.removeAuth(authTokenError));
    }

    @Test
    void getAuth() throws DataAccessException {
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        String authToken = "exaToken";
        AuthData starterAuth = new AuthData(authToken, "exaUsername");
        sqlAuthDao.createAuth(starterAuth);

        String finalAuth = sqlAuthDao.getAuth(authToken);
        assertEquals(authToken, finalAuth);
    }

    @Test
    void getAuthInvalid() throws DataAccessException {
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        String authToken = "exaToken";
        String authTokenError = "ErrorToken";
        AuthData starterAuth = new AuthData(authToken, "exaUsername");
        sqlAuthDao.createAuth(starterAuth);

        Assertions.assertDoesNotThrow(()->sqlAuthDao.getAuth(authTokenError));
    }


    @Test
    void clear() throws DataAccessException, SQLException {
        SQLAuthDAO sqlAuthDao = new SQLAuthDAO();
        String authToken = "exaToken";
        AuthData starterAuth = new AuthData(authToken, "exaUsername");
        sqlAuthDao.createAuth(starterAuth);
        sqlAuthDao.clear();
        databaseConnection(starterAuth);
    }
    private void databaseConnection(AuthData starterAuth) throws DataAccessException, SQLException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var statement = conn.prepareStatement("SELECT username, authToken FROM auth WHERE username=?")) {
                statement.setString(1, starterAuth.username());
                try (var table = statement.executeQuery()) {
                    assertFalse(table.next());
                }
            }
        }
    }



}
