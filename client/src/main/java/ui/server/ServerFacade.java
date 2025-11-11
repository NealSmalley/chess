package ui.server;

import chess.ChessGame;
import com.google.gson.Gson;
import ui.exception.DataAccessException;


import java.net.URI;
import java.net.http.*;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse.BodyHandlers;

public class ServerFacade {
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(String url){
        serverUrl = url;
    }
    public ChessGame create(String gameName) throws DataAccessException {
        Gson serializer = new Gson();
        String gameNameSerialized = serializer.toJson(gameName);
        HttpRequest req = buildRequest("POST","/game", gameNameSerialized);
        HttpResponse<String> resServer = sendRequest(req);
        //What is the responseClass suppose to be?
        return handleResponse(resServer, null);
    }

    public void list(){

    }



    private HttpRequest buildRequest(String method, String path, Object body) {
        //newBuilder() allows for configure/modify http request before sending
        HttpRequest.Builder builder = HttpRequest.newBuilder();
        //creates a uri which is similar to a url
        builder.uri(URI.create(serverUrl+path));
        //chooses method (exa: POST) and then creates the request body
        builder.method(method, makeRequestBody(body));
        //sets header
        if (body != null) {
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
            throw new DataAccessException(DataAccessException.PosExc.ServerError, ex.getMessage());
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
            throw new DataAccessException(DataAccessException.fromHttpStatusCode(status),"another failure: "+status);
        }
        if (responseClass != null){
            return new Gson().fromJson(response.body(), responseClass);
        }
        return null;
    }

    private boolean isSuccessful(int status) {
        //any status that starts with 200
        return status/100 == 2;
    }

}
