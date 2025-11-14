package client;

import model.client.LoginData;
import model.*;
import org.junit.jupiter.api.*;
import server.Server;
import ui.exception.DataAccessException;
import ui.server.ServerFacade;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;


public class ServerFacadeTests {
    private Map<Integer, Integer> gameNumberMap = new HashMap<>();

    private static Server server;
    private ServerFacade serverFacade;
    private static int port;

    @BeforeAll
    public static void init() {
        server = new Server();
        port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void sampleTest() {
        assertTrue(true);
    }

    @BeforeEach
    public void serverFacadeCreation() throws Exception{
        serverFacade = new ServerFacade("http://localhost:"+port);
        serverFacade.clearApplication();
    }
    @Test
    void clearApplicationValid() throws Exception{
        registerValid();
        assertDoesNotThrow(() -> serverFacade.clearApplication());
    }


    @Test
    void registerValid() throws Exception {
        //serverFacade = new ServerFacade("http://localhost:8080");
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = serverFacade.register(userData);
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void registerInvalid() throws Exception {
        UserData userData = new UserData("player1", "password", "p1@email.com");
        AuthData authData = serverFacade.register(userData);
        assertThrows(DataAccessException.class, () -> serverFacade.register(userData));
    }

    @Test
    void logoutValid() throws Exception {
        registerValid();
        assertDoesNotThrow(() -> serverFacade.logout());
    }

    @Test
    void logoutInvalid() throws Exception {
        assertThrows(DataAccessException.class, () -> serverFacade.logout());
    }


    @Test
    void loginValid() throws Exception {
        registerValid();
        LoginData loginData = new LoginData("player1", "password");
        AuthData authData = serverFacade.login(loginData);
        assertFalse(authData.authToken().isEmpty());
    }

    @Test
    void loginInvalid() throws Exception {
        registerValid();
        LoginData loginData = new LoginData("player1", null);
        assertThrows(DataAccessException.class, () -> serverFacade.login(loginData));
    }

    @Test
    void createGameValid() throws Exception {
        registerValid();
        String gameName = "unitTestGame";
        GameData gameData = serverFacade.createGame(gameName);
        assertNotNull(gameData);
    }

    @Test
    void createGameInvalid() throws Exception {
        registerValid();
        String gameName = null;
        assertThrows(DataAccessException.class, () -> serverFacade.createGame(gameName));
    }

    @Test
    void listGameValid() throws Exception {
        createGameValid();
        GameList gameList = serverFacade.listGame();
        assertNotNull(gameList);


        int gameNumber = 0;
        for (GameData game : gameList.games()){
            int gameid = game.gameID();
            gameNumber++;
            gameNumberMap.put(gameNumber, gameid);
        }
    }

    @Test
    void listGameInvalid() throws Exception {
        assertThrows(DataAccessException.class, () -> serverFacade.listGame());
    }

    @Test
    void joinGameValid() throws Exception {
        listGameValid();
        assertDoesNotThrow(() -> serverFacade.join(1, "white", gameNumberMap));
    }
    @Test
    void joinGameInvalid() throws Exception {
        listGameValid();
        serverFacade.join(1, "white", gameNumberMap);
        assertThrows(DataAccessException.class,() -> serverFacade.join(1, "white", gameNumberMap));
    }








}