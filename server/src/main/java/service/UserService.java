package service;

import dataaccess.DataAccess;
import dataaccess.MightNeed.AuthDAO;
import dataaccess.MightNeed.UserDAO;
import model.AuthData;
import model.UserData;


public class UserService {
    private final UserDAO userDao;
    private final AuthDAO authDao;
    public UserService(UserDAO userDao, AuthDAO authDao){
        this.userDao = userDao;
        this.authDao = authDao;
    }
    public AuthData register(UserData user) throws Exception{
        AuthData auth = new AuthData(user.username(),generateAuthToken());
        //line 4
        if (userDao.getUser(user.username()) == null){
            //line 6 & 7
            throw new Exception("already exists");
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
    private String generateAuthToken() {
    return "xyz";
    }
}
