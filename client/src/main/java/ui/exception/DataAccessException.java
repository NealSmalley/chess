package ui.exception;

import com.google.gson.Gson;

import java.util.HashMap;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{

    //possible Exceptions
    public enum PosExc {
        BadRequest,
        Unauthorized,
        Forbidden,
        ServerError,
        ClientError,
        NullValue;
    }
    //Exception private var
    //private final PosExc exc;

    public DataAccessException(String message) {
        super(message);
    }

//    public DataAccessException(PosExc exc, String message) {
//        super(message);
//        this.exc = exc;
//    }
//    public DataAccessException(PosExc exc, String message, Throwable ex) {
//        super(message, ex);
//        this.exc = exc;
//    }
    //getter
//    public PosExc getExc(){
//        return exc;
//    }

    //fromJson
    public static DataAccessException fromJson(String json){
        HashMap map = new Gson().fromJson(json, HashMap.class);
        //this might throw an error because I didn't label it status elsewhere?
        //var status = PosExc.valueOf(map.get("status").toString());
        String message = map.get("message").toString();
        return new DataAccessException(message);
    }

    //fromHttpStatusCode
    public static PosExc fromHttpStatusCode(int httpStatusCode){
        return switch (httpStatusCode) {
            case 500 -> PosExc.ServerError;
            case 400 -> PosExc.ClientError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }

}
