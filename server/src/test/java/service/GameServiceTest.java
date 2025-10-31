package service;

import com.google.gson.Gson;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import server.BadRequestException;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class GameServiceTest {

    AuthData preReq() throws Exception{
        var user = new UserData("joe","j@J.com", "toomanysecrets");
        UserDAO db = new MemoryUserDAO();
        AuthDAO auth = new MemoryAuthDAO();
        var userService = new UserService(db,auth);
        var authDataReg = userService.register(user);
        AuthData authDataLog = userService.login(user);
        return authDataLog;
    }

    @Test
    void createGame() throws Exception{
        AuthData authData = preReq();
        GameDAO gameDao = new MemoryGameDAO();
        AuthDAO authDao = new MemoryAuthDAO();
        authDao.createAuth(authData);
        var gameService = new GameService(gameDao, authDao);
        String gameName = "Test";
        Gson gson = new Gson();
        String gameNamejson = gson.toJson(Map.of("gameName", gameName));
        var gameDataResult = gameService.creategame(gameNamejson);

        //Do I need to add more?
        assertNotNull(gameDataResult);
        //assertEquals(user.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void createGameInvalid() throws Exception{
        AuthData authData = preReq();
        GameDAO gameData = new MemoryGameDAO();
        AuthDAO authDao = new MemoryAuthDAO();
        authDao.createAuth(authData);
        var gameService = new GameService(gameData, authDao);
        String gameName = "";

        //assertNull(gameDataResult);
        Assertions.assertThrows(BadRequestException.class, ()->gameService.creategame(gameName));
        //assertEquals(user.username(), authData.username());
        //assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void joinGame() throws Exception{
        AuthData authData = preReq();
        GameDAO gameData = new MemoryGameDAO();
        AuthDAO authDao = new MemoryAuthDAO();
        authDao.createAuth(authData);
        var gameService = new GameService(gameData, authDao);
        String gameName = "Test";
        Gson gson = new Gson();
        String gameNamejson = gson.toJson(Map.of("gameName", gameName));
        gameService.creategame(gameNamejson);

        int gameID = 1;
        String playerColor = "BLACK";
        String username = "joe";
        GameData updatedGame = gameService.updategame(gameID, playerColor, username);

        assertNotNull(updatedGame);
        //Assertions.assertThrows(UnauthorizedException.class, ()->gameService.updategame(gameID, playerColor, username));
        //assertEquals(user.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void joinGameInvalid() throws Exception{
        AuthData authData = preReq();
        GameDAO gameData = new MemoryGameDAO();
        AuthDAO authDao = new MemoryAuthDAO();
        authDao.createAuth(authData);
        var gameService = new GameService(gameData, authDao);
        String gameName = "Test";
        Gson gson = new Gson();
        String gameNamejson = gson.toJson(Map.of("gameName", gameName));
        gameService.creategame(gameNamejson);
        //var gameDataResult = gameService.creategame(gameName);

        int gameID = 1;
        String playerColor = "BLACK";
        String username = "Tester";
        GameData updatedGame = gameService.updategame(gameID, playerColor, username);

        //What tests need to be done?
        //assertNotNull(updatedGame);
        Assertions.assertThrows(Exception.class, ()->gameService.updategame(gameID, playerColor, username));
        //assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void listGames() throws Exception{
        AuthData authData = preReq();
        GameDAO gameData = new MemoryGameDAO();
        AuthDAO authDao = new MemoryAuthDAO();
        authDao.createAuth(authData);
        var gameService = new GameService(gameData, authDao);
        String gameName = "Test";
        Gson gson = new Gson();
        String gameNamejson = gson.toJson(Map.of("gameName", gameName));

        var gameDataResult = gameService.creategame(gameNamejson);

        HashMap<Integer, GameData> games = gameService.listgames();

        //What tests need to be done?
        assertNotNull(games);
        //assertEquals(user.username(), authData.username());
        assertTrue(!authData.authToken().isEmpty());
    }

    @Test
    void listGamesInvalid() throws Exception{
        AuthData authData = preReq();
        GameDAO gameData = new MemoryGameDAO();
        AuthDAO authDao = new MemoryAuthDAO();
        authDao.createAuth(authData);
        var gameService = new GameService(gameData, authDao);
        String gameName = "Test";
        Gson gson = new Gson();
        String gameNamejson = gson.toJson(Map.of("gameName", gameName));
        var gameDataResult = gameService.creategame(gameNamejson);

        HashMap<Integer, GameData> games = gameService.listgames();
        games.clear();

        assertNotNull(games);
        Assertions.assertThrows(Exception.class, ()->{
            gameService.listgames();
            UserDAO db = new MemoryUserDAO();
            AuthDAO auth = new MemoryAuthDAO();
            var userService = new UserService(db,auth);
            userService.authenticatUser(null);
        });

    }


}
