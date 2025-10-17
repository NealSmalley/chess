//package service;
//
//import model.UserData;
//import org.junit.jupiter.api.Test;
//
//import static org.junit.jupiter.api.Assertions.*;
//
//class DataAccessTest {
//    @Test
//    void clear(){
//        DataAccess db = new MemoryDataAccess();
//        db.createUser(new UserData("joe","j@J.com", "toomanysecrets"));
//        db.clear();
//        assertNull(db.getUser("joe"));
//    }
//    @Test
//    void creatUser(){
//        DataAccess db = new MemoryDataAccess();
//        var user = new UserData("joe","j@J.com", "toomanysecrets")
//        db.createUser(user);
//        assertEquals(user, db.getUser(user.username()));
//    }
//}