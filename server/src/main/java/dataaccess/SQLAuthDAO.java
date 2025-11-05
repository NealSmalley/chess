package dataaccess;

import model.*;
//imports Connection and SQLException
import java.sql.*;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;



public class SQLAuthDAO implements AuthDAO{
    //Constructor
    public SQLAuthDAO() throws DataAccessException {
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
        catch (dataaccess.DataAccessException ex) {
            throw new DataAccessException(dataaccess.DataAccessException.PossibleExc.ServerError, "500: ServerError exception: database error");
        }

    }
    //var with SQL table
    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  auth (
              `authToken` varchar(256) NOT NULL,
              `username` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(username)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci
            """
    };

    public void createAuth(AuthData authData) throws DataAccessException {
        var statement = "INSERT INTO auth (authToken, username) VALUES (?, ?)";
        int id = executeUpdate(statement, authData.authToken(), authData.username());
    }

    private int executeUpdate(String statement, Object... params) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(statement, RETURN_GENERATED_KEYS)) {
                for (int i = 0; i < params.length; i++) {
                    Object param = params[i];
                    if (param instanceof String p) {ps.setString(i + 1, p);}
                    //might need later but for now I don't
//                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
//                    else if (param instanceof PetType p) ps.setString(i + 1, p.toString());
                    else if (param == null) {ps.setNull(i + 1, NULL);}
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
        catch (DataAccessException ex){
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
    }


    public void clear() throws DataAccessException {
            String statement = "TRUNCATE auth";
            executeUpdate(statement);
    }

    public String getAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken, username FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                //1 refers to the first questions mark
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
        return null;
    }
    private String readAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        AuthData auth = new AuthData(authToken, username);
        return auth.authToken();
    }
    private String readUsernameAuth(ResultSet rs) throws SQLException {
        var authToken = rs.getString("authToken");
        var username = rs.getString("username");
        return username;
    }

    public void removeAuth(String authToken) throws DataAccessException {
            var statement = "DELETE FROM auth WHERE authToken=?";
            executeUpdate(statement, authToken);
    }

    public String getUsernameAuth(String authToken) throws DataAccessException {
        try (Connection conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, authToken FROM auth WHERE authToken=?";
            try (PreparedStatement ps = conn.prepareStatement(statement)) {
                //1 refers to the first questions mark
                ps.setString(1, authToken);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readUsernameAuth(rs);
                    }
                }
            }
        } catch (Exception e) {
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "500: getUser method serverError");
        }
        return null;
    }

}
