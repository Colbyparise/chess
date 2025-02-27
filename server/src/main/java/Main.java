import chess.*;
import server.Server;
import service.GameService;
import service.UserService;

public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);

        UserService userService = new UserService();
        GameService gameService = new GameService();

        Server server = new Server(userService, gameService);
        int port = server.run(8080);

        System.out.println("Server started on port: " + port);
    }
}