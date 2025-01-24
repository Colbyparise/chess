package chess.PieceMovement;
import chess.*;
import java.util.HashSet;

public class QueenMovesCalculator implements PieceMovesCalculator {

    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        int X = position.getColumn();
        int Y = position.getRow();

        //possible queen moves
        int[][] directions = {
                {0, 1},
                {1, 0},
                {0, -1},
                {-1, 0},
                {-1, -1},
                {-1, 1},
                {1, -1},
                {1, 1}
        };
        ChessGame.TeamColor team = board.getSquareTeam(position);
        return PieceMovesCalculator.DirectionalMoves(board, position, directions, Y, X, team);
    }
}