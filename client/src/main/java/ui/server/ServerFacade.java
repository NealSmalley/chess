package ui.server;

import com.google.gson.Gson;
import model.*;
import model.client.LoginData;
import ui.exception.DataAccessException;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.Map;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;
    private String authToken;


    public ServerFacade(String url){
        serverUrl = url;
    }

    public GameData createGame(String gameName) throws DataAccessException {
        GameName gameNameObj = new GameName(gameName);
        HttpRequest req = buildRequest("POST","/game", gameNameObj);
        HttpResponse<String> resServer = sendRequest(req);
        return handleResponse(resServer, GameData.class);
    }

    public GameList listGame() throws DataAccessException{
        HttpRequest req = buildRequest("GET", "/game", null);
        HttpResponse<String> resServer = sendRequest(req);
        return handleResponse(resServer, GameList.class);
    }

    public AuthData register(UserData userdata) throws DataAccessException{
        HttpRequest req = buildRequest("POST", "/user", userdata);
        HttpResponse<String> resServer = sendRequest(req);
        AuthData authdata = handleResponse(resServer, AuthData.class);
        authToken = authdata.authToken();
        return authdata;
    }

    public AuthData login(LoginData logindata) throws DataAccessException {
        HttpRequest req = buildRequest("POST", "/session", logindata);
        HttpResponse<String> resServer = sendRequest(req);
        AuthData authdata = handleResponse(resServer, AuthData.class);
        authToken = authdata.authToken();
        return authdata;
    }
    public void join(int gameNumber, String color, Map<Integer, Integer> gameNumberMap) throws DataAccessException{
        int gameID = gameNumberMap.get(gameNumber);
        String colorUpperCase = color.toUpperCase();
        JoinGameData joinGameData = new JoinGameData(gameID, colorUpperCase);
        HttpRequest req = buildRequest("PUT", "/game", joinGameData);
        HttpResponse<String> resServer = sendRequest(req);
        handleResponse(resServer, null);
    }
    public void logout() throws DataAccessException{
        //do I need to incorporate authTokens in the header
        HttpRequest req = buildRequest("DELETE", "/session", null);
        HttpResponse<String> resServer = sendRequest(req);
        handleResponse(resServer, null);
    }
    public void clearApplication() throws DataAccessException{
        HttpRequest req = buildRequest("DELETE", "/db", null);
        HttpResponse<String> resServer = sendRequest(req);
        handleResponse(resServer, null);
    }


    private HttpRequest buildRequest(String method, String path, Object body) {
        //newBuilder() allows for configure/modify http request before sending
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        //creates a uri which is similar to a url
        builder.uri(URI.create(serverUrl+path));
        //chooses method (exa: POST) and then creates the request body
        builder.method(method, makeRequestBody(body));
        //sets header
        if ((body != null) && (authToken == null)) {
            builder.setHeader("Content-Type", "application/json");
        }
        //this was an else statement
        else if (authToken != null){
            builder.setHeader("authorization", authToken);
        }
        //clear application
        else {
            builder.setHeader("Content-Type", "application/json");
        }
        //builds json request
        return builder.build();
    }

    private BodyPublisher makeRequestBody(Object request){
        if (request != null){
            return BodyPublishers.ofString(new Gson().toJson(request));
        } else {
            return BodyPublishers.noBody();
        }
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws DataAccessException {
        //sends req and returns server response
            //bodyHandlers.ofString converts http to java string
        try {
            return client.send(request, BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new DataAccessException(ex.getMessage());
        }
    }
    //handlesResponses: exceptions, not successful and success
    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws DataAccessException {
        int status = response.statusCode();
        if (!isSuccessful(status)) {
            var body = response.body();
            if (body != null) {
                throw DataAccessException.fromJson(body);
            }
            throw new DataAccessException("another failure: "+status);
        }
        //data in the body?
        if (responseClass != null){
            //returns as the responseClass
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {
        //any status that starts with 200
        return status/100 == 2;
    }

}
