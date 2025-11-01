package dataaccess;

import model.AuthData;
import service.UnauthorizedException;

public interface AuthDAO {
    //line 11
    public void createAuth(AuthData authData) throws DataAccessException;
    public void clear() throws DataAccessException;
    public String getAuth(String authToken) throws DataAccessException;
    public void removeAuth(String authToken) throws DataAccessException;
    public String getUsernameAuth(String authToken) throws DataAccessException;
}
