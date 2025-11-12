package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{

    //possible Exceptions
    public enum PosExc {
        BadRequest,
        Unauthorized,
        Forbidden,
        ServerError
    }
    //Exception private var
    private final PosExc exc;

    public DataAccessException(PosExc exc, String message) {
        super(message);
        this.exc = exc;
    }
    public DataAccessException(PosExc exc, String message, Throwable ex) {
        super(message, ex);
        this.exc = exc;
    }
    //getter
    public PosExc getExc(){
        return exc;
    }
}