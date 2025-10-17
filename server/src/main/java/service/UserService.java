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
    public AuthData register(UserData user) throws Exception, ValidationException, JsonSyntaxException {
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

    public void clear() {
        authDao.clear();
    }

    private String generateAuthToken() {
            return UUID.randomUUID().toString();
        }
}
