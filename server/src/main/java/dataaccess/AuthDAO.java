package dataaccess;

import model.AuthData;
import service.UnauthorizedException;

public interface AuthDAO {
    //line 11
    public void createAuth(AuthData authData);
    public void clear();
    public String getAuth(String authToken) throws UnauthorizedException;
    public void removeAuth(String authToken);
    public String getUsernameAuth(String authToken);
}
