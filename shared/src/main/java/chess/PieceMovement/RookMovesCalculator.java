package chess.PieceMovement;
import chess.*;
import java.util.HashSet;

public class RookMovesCalculator implements PieceMovesCalculator {

    //possible rook moves
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        int X = position.getColumn();
        int Y = position.getRow();
        int[][] directions = {
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1}
        };
        ChessGame.TeamColor team = board.getSquareTeam(position);

        return PieceMovesCalculator.DirectionalMoves(board, position, directions, Y, X, team);
    }
}