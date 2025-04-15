package network.http;

public record JoinGame(String authToken, String playerColor, int gameID) {
}