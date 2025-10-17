//package service;
//
//import model.UserData;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class UserServiceTest {
//
//    @Test
//    void register() throws Exception{
//        DataAccess db = new MemoryDataAccess();
//        var user = new UserData("joe","j@J.com", "toomanysecrets")
//        var userService = new UserService(db);
//        var authData = userService.register(user);
//        assertNotNull(authData);
//        assertEquals(user.username(), authData.username());
//        assertTrue(!authData.authToken().isEmpty());
//    }
//
//    @Test
//    void registerInvalidUsername() throws Exception{
//        DataAccess db = new MemoryDataAccess();
//        var user = new UserData(null,"j@J.com", "toomanysecrets")
//        var userService = new UserService(db);
//        var authData = userService.register(user);
//        assertNotNull(authData);
//        assertEquals(user.username(), authData.username());
//        assertTrue(!authData.authToken().isEmpty());
//    }
//
//    @Test
//    void clear() {
//    }
//}