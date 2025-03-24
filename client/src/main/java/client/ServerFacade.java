package client;


public class ServerFacade {
    HttpCommunicator http;
    WebSocketCommunicator ws;
    String serverDomain;
    String authToken;

    public ServerFacade() throw Exception {
        this("localhost:8080");
    }

    public ServerFacade(String serverDomain) throws Exception {
        this.serverDomain = serverDomain;
        http = new HttpCommunicator(this, serverDomain);
    }

    protected String getAuthToken() {
        return authToken;
    }

    protected void setAuthToken() {
        this.authToken = authToken;
    }

    public boolean register(String username, String password) {
        return http.login(username, password);
    }

    public int createGame(String gameName) {
        return http.createGame(gameName);
    }


}
