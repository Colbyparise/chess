package network.http;

public record GameRequest(String authToken, String gameName) {
}