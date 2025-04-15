package dataaccess;

/**
 * Indicates there was an error connecting to the database
 */
public class UnauthorizedException extends DataAccessException{
    public UnauthorizedException(String message) {
        super(message);
    }
}