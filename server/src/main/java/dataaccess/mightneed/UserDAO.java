package dataaccess.mightneed;

import model.UserData;

public interface UserDAO {
    void clear();
    //line 5
    UserData getUser(String username);
    //line 9
    void createUser(UserData user);
}
