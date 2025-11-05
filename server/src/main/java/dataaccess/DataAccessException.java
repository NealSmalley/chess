package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class DataAccessException extends Exception{
//    public DataAccessException(String message) {
//        super(message);
//    }
//    public DataAccessException(String message, Throwable ex) {
//        super(message, ex);
//    }
    //old version
    //public DataAccessException(){
    //      super();
    //    }


    //possible Exceptions
    public enum PossibleExc {
        BadRequest,
        Unauthorized,
        Forbidden,
        ServerError
    }
    //Exception private var
    private final DataAccessException.PossibleExc exc;

    public DataAccessException(DataAccessException.PossibleExc exc, String message) {
        super(message);
        this.exc = exc;
    }
    public DataAccessException(DataAccessException.PossibleExc exc, String message, Throwable ex) {
        super(message, ex);
        this.exc = exc;
    }
    //getter
    public PossibleExc getExc(){
        return exc;
    }
}