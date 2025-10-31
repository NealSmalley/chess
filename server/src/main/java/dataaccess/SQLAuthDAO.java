package dataaccess;

import exception.ResponseException;


public class SQLAuthDAO implements AuthDAO{
    //Constructor
    public SQLAuthDAO() throws ResponseException {
        initDatabase();
    }
    //Creates Database
    private void initDatabase() throws ResponseException {
        DatabaseManager.createDatabase();
        try (Connection conn = DatabaseManager.getConnection()) {
            for (String statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {
            throw new ResponseException(ResponseException.Code.ServerError, String.format("Unable to configure database: %s", ex.getMessage()));
        }



}
