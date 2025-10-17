package dataaccess.MightNeed;

import model.AuthData;
import model.UserData;

import java.util.HashMap;

public class MemoryAuthDAO implements AuthDAO{
    private final HashMap<String, AuthData> auths = new HashMap<>();

    //line 12
    @Override
    public void createAuth(AuthData auth){
        auths.put(auth.authToken(), auth);
    }

    @Override
    public void clear(){
        auths.clear();
    }
}
