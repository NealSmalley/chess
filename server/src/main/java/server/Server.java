package server;

import com.google.gson.Gson;
//import dataaccess.MemoryDataAccess;
import com.google.gson.JsonSyntaxException;
import dataaccess.MightNeed.AuthDAO;
import dataaccess.MightNeed.MemoryAuthDAO;
import dataaccess.MightNeed.MemoryUserDAO;
import dataaccess.MightNeed.UserDAO;
import io.javalin.*;
import io.javalin.http.*;

import io.javalin.validation.ValidationException;
import model.UserData;

import service.UserService;

import java.util.Map;

public class Server {

    private final Javalin javalin;
    private final UserDAO userDao;
    private final UserService userService;
    private final AuthDAO authDao;

    public Server() {
        this.userDao = new MemoryUserDAO();
        this.authDao = new MemoryAuthDAO();
        this.userService = new UserService(userDao, authDao);
        //var userService = new UserService(dataAccess);

        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        //line 1
        javalin.post("/user", this::register);
//        javalin.post("/session", this::login);
//        javalin.delete("/session", this::logout);
//        javalin.get("/game", this::listGame);
//        javalin.post("/game", this::creatGame);
//        javalin.put("/game", this::joinGame);
        javalin.delete("/db", this::clearApplication);

    }
// handler (whole method)
    private void register(Context ctx) {
        //req = request, res = response
        try {
            Gson serializer = new Gson();
            //line 2
            //reads Json req body
            String reqJson = ctx.body();
            UserData req = serializer.fromJson(reqJson, UserData.class);
            //pass back an AuthData
            if (req.username() == null || req.email() == null || req.password() == null){
                throw new BadRequestException();
            }

            // call to the service and register
            //line 3
            model.AuthData authData = userService.register(req);
            //line 13
            ctx.result(serializer.toJson(authData));
            //var msg = String.format("{\"username\":\"\",\"authToken\":\"\"}");
            ctx.status(200).result();
            //var res = Map.of("username", req.username(), "authToken", "yzx");
            //var res = serializer.toJson(new authData("username", req.username(), "authToken", "yzx"));

            //ctx.status(200).json(res);

        }
        //400
        catch (BadRequestException ex){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            ctx.status(400).json(msg);
        }
        //403
        catch (Exception ex){
            var msg = String.format("{\"message\": \"Error: %s\"}", ex.getMessage());
            //ctx.status(403).result();
            ctx.status(403).json(msg);
        }
    }
    //hander
    private void clearApplication(Context ctx) {
        userService.clear();
        ctx.status(200);
    }


        public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }
}
