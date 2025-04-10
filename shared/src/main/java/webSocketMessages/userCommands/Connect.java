package webSocketMessages.userCommands;

public class Connect extends UserGameCommand {

    public Connect(String authToken, int gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }
}

