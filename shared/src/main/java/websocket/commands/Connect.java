package websocket.commands;

public class Connect extends UserGameCommand {

    public Connect(String authToken, int gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }

    // Empty constructor required for Gson
    public Connect() {
        super(CommandType.CONNECT, null, null);
    }
}
