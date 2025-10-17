package dataaccess.MightNeed;

import model.AuthData;

public interface AuthDAO {
    //line 11
    public void createAuth(AuthData authData);
    public void clear();
}
