package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UserServiceTest {

    @Test
    void register() throws Exception{
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
        var user = new UserData("joe","j@J.com", "toomanysecrets");
        var userService = new UserService(db,auth);
        var authData = userService.register(user);
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void registerInvalidUsername() throws Exception{
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
        var user = new UserData("joe","j@J.com", "toomanysecrets");
        var userService = new UserService(db,auth);
        var authData = userService.register(user);

        Assertions.assertThrows(Exception.class,()->userService.register(user));
    }

    @Test
    void login() throws Exception{
        var user = new UserData("joe","j@J.com", "toomanysecrets");
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
        var userService = new UserService(db,auth);
        var authDataReg = userService.register(user);
        var authData = userService.login(user);
        assertNotNull(authData);
        assertEquals(user.username(), authData.username());
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void loginInvalidUsername() throws Exception{
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
        var user = new UserData(null,"j@J.com", "toomanysecrets");
        var userService = new UserService(db,auth);
        //var authDataReg = userService.register(user);
        //var authData = userService.login(user);
        Assertions.assertThrows(DataAccessException.class,()->userService.login(user));
    }

    @Test
    void logout() throws Exception{
        var user = new UserData("joe","j@J.com", "toomanysecrets");
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
        var userService = new UserService(db,auth);
        var authDataReg = userService.register(user);
        var authDataLog = userService.login(user);

        Assertions.assertDoesNotThrow(()->userService.logout(authDataLog));
    }

    @Test
    void logoutInvalid() throws Exception{
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
//        var user = new UserData(null,"j@J.com", "toomanysecrets");
        var userService = new UserService(db,auth);
//        var authData = userService.login(user);

        //idea 2
        AuthData unlikelyAuthData = new AuthData("unlikely token", "unlikely username");

        Assertions.assertThrows(dataaccess.DataAccessException.class, ()->userService.logout(unlikelyAuthData));
    }


    @Test
    void clear() {
    }
}