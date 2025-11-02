package dataaccess;

import model.UserData;

public interface UserDAO {
    void clear() throws DataAccessException;
    //line 5
    UserData getUser(String username) throws DataAccessException;
    //line 9
    void createUser(UserData user) throws DataAccessException;
}
