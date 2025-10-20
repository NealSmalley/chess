package service;

import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import dataaccess.MightNeed.AuthDAO;
import dataaccess.MightNeed.UserDAO;
import io.javalin.validation.ValidationException;
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
            throw new DataAccessException("already taken");
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

    public void logout(AuthData auth) throws UnauthorizedException {
    //if logged in
        String authTokenUser = auth.authToken();
        String authTokenDB = authDao.getAuth(authTokenUser);
        if (!(authTokenDB.equals(authTokenUser))){
            throw new UnauthorizedException();
        }
        //remove authToken
        authDao.removeAuth(authTokenUser);
    }
    //checks of authentication token is valid
    public void authenticatUser(String authTokenUser) throws UnauthorizedException{
        String authTokenDB = authDao.getAuth(authTokenUser);
        if (!(authTokenDB.equals(authTokenUser))){
            throw new UnauthorizedException();
        }
    }


    public void clear() {
        authDao.clear();
        userDao.clear();
    }

    private String generateAuthToken() {
            return UUID.randomUUID().toString();
        }
}
