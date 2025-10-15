//package dataaccess;
//
//
//import model.AuthData;
//import model.UserData;
//
//import java.util.HashMap;
//
//public class MemoryDataAccess implements DataAccess{
//    private final HashMap<String, UserData> users = new HashMap<>();
//    @Override
//    public void clear(){
//        users.clear();
//    }
//    //line 5
//    @Override
//    public UserData getUser(String username){
//        return users.get(username);
//    }
//    //line 9
//    @Override
//    public void createUser(UserData user){
//        users.put(user.username(), user);
//    }
//    //line 11
//    @Override
//    public void createAuth(AuthData authData){
//        users.put(auth.authData, auth);
//    }
//
//}