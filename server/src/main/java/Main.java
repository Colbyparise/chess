import chess.*;
import dataaccess.DataAccessException;
import dataaccess.DatabaseManager;
import server.Server;

public class Main {

    public static void main(String[] args) {
        try {
            DatabaseManager.createDatabase(); } catch(DataAccessException exception){
            throw new RuntimeException(exception);
        }
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("♕ 240 Chess Server: " + piece);
        Server server = new Server();
        int port = server.run(8080);
    }
}