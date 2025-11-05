package service;

import dataaccess.AuthDAO;
import dataaccess.UserDAO;
import dataaccess.DataAccessException;

import model.AuthData;
import model.UserData;
import org.mindrot.jbcrypt.BCrypt;


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
            throw new dataaccess.DataAccessException(dataaccess.DataAccessException.PossibleExc.Forbidden, "already taken");
        }
        else {
            try {
                String hashedPassword = BCrypt.hashpw(user.password(), BCrypt.gensalt());
                var encryptedUser = new UserData(user.username(), hashedPassword, user.email());


                //line 9
                userDao.createUser(encryptedUser);
                //line 11
                authDao.createAuth(auth);
            }
            catch (DataAccessException ex) {
                throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, "400: unable to register: register method");
            }
        }
        //line 12
        return auth;
    }
    public AuthData login(UserData user) throws Exception {
        AuthData auth = new AuthData(generateAuthToken(),user.username());
        //User doesn't exist
        if (userDao.getUser(user.username()) == null) {
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "user exists already");
        }
        //User exists
        else {
            UserData userdata = userDao.getUser(user.username());
            //encoded version
            if (BCrypt.checkpw(user.password(),userdata.password())){
                authDao.createAuth(auth);
            }
            else{
                throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "Password is wrong");
            }
        }
        return auth;
    }

    public void logout(AuthData auth) throws DataAccessException {
    //if logged in
        String authTokenUser = auth.authToken();
        String authTokenDB = authDao.getAuth(authTokenUser);
        if (authTokenDB == null){
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "authToken in null");
        }
        else if (!(authTokenDB.equals(authTokenUser))){
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "authTokens don't match");
        }
        //remove authToken
        authDao.removeAuth(authTokenUser);
    }
    //checks of authentication token is valid
    public void authenticatUser(String authTokenUser) throws DataAccessException {
        String authTokenDB = authDao.getAuth(authTokenUser);
        if (authTokenDB == null){
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "authTokens don't match");
        }
        if (!(authTokenDB.equals(authTokenUser))){
            throw new DataAccessException(DataAccessException.PossibleExc.Unauthorized, "authTokens don't match");
        }
    }
    public String getUsernameAuth(String authToken) throws DataAccessException {
        String username = authDao.getUsernameAuth(authToken);
        if (username != null) {
            return username;
        }
        else{
            throw new DataAccessException(DataAccessException.PossibleExc.BadRequest, "username is null");

        }

    }


    public void clear() throws DataAccessException {
        authDao.clear();
        userDao.clear();
    }

    private String generateAuthToken() {
            return UUID.randomUUID().toString();
        }
}
