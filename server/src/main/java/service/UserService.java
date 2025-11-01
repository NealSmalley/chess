package service;

import dataaccess.DataAccessException;
import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;


public class UserService {
    private final UserDAO userDao;
    private final AuthDAO authDao;
    public UserService(UserDAO userDao, AuthDAO authDao){
        this.userDao = userDao;
        this.authDao = authDao;
    }
    public AuthData register(UserData user) throws Exception {
        AuthData auth = new AuthData(generateAuthToken(),user.username());
        //line 4
        if (userDao.getUser(user.username()) != null){
            //line 6 & 7
            throw new DataAccessException(DataAccessException.PossibleExc.ServerError, "already taken");
        }
        else {
            //line 9
            userDao.createUser(user);
            //line 11
            authDao.createAuth(auth);
        }
        //line 12
        return auth;
    }
    public AuthData login(UserData user) throws Exception {
        AuthData auth = new AuthData(generateAuthToken(),user.username());
        //User exists
        if (userDao.getUser(user.username()) == null) {
            throw new UnauthorizedException();
        }
        else {
            UserData userdata = userDao.getUser(user.username());
            //password is wrong
            if (!(userdata.password().equals(user.password()))){
                throw new UnauthorizedException();
            }
            authDao.createAuth(auth);
        }
        return auth;
    }

    public void logout(AuthData auth) throws DataAccessException {
    //if logged in
        String authTokenUser = auth.authToken();
        String authTokenDB = authDao.getAuth(authTokenUser);
        if (!(authTokenDB.equals(authTokenUser))){
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "authTokens don't match");
        }
        //remove authToken
        authDao.removeAuth(authTokenUser);
    }
    //checks of authentication token is valid
    public void authenticatUser(String authTokenUser) throws DataAccessException{
        String authTokenDB = authDao.getAuth(authTokenUser);
        if (!(authTokenDB.equals(authTokenUser))){
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "authTokens don't match");
        }
    }
    public String getUsernameAuth(String authToken) throws DataAccessException{
        String username = authDao.getUsernameAuth(authToken);
        if (username != null) {
            return username;
        }
        else{
            throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, "unable to find username");

        }

    }


    public void clear() throws DataAccessException{
        authDao.clear();
        userDao.clear();
    }

    private String generateAuthToken() {
            return UUID.randomUUID().toString();
        }
}
