package dataaccess;

import model.AuthData;
import service.UnauthorizedException;

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
    @Override
    public String getAuth(String authTokenkey) throws DataAccessException{
        AuthData authData = auths.get(authTokenkey);
        String authToken;
        if (authData != null) {
            authToken = authData.authToken();
        }
        else{
            throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, "AuthData is null");

        }
        return authToken;
    }

    @Override
    public void removeAuth(String authToken){
        auths.remove(authToken);
    }
    @Override
    public String getUsernameAuth(String authToken){
        AuthData authData = auths.get(authToken);
        return authData.username();
    }

}
