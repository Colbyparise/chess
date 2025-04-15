package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class TakenException extends DataAccessException{
    public TakenException(String message) {
        super(message);
    }
}