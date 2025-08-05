package websocket.commands;


public class JoinObserver extends UserGameCommand {

    public JoinObserver(String authToken, int gameID) {
        super(CommandType.CONNECT, authToken, gameID);
    }
}