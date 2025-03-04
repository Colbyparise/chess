package chess.piecemovement;
import chess.*;
import java.util.HashSet;

public class    RookMovesCalculator implements PieceMovesCalculator {

    //possible rook moves
    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        int x = position.getColumn();
        int y = position.getRow();
        int[][] directions = {
                {0, 1},
                {1, 0},
                {-1, 0},
                {0, -1}
        };
        ChessGame.TeamColor team = board.getSquareTeam(position);

        return PieceMovesCalculator.directionalmoves(board, position, directions, y, x, team);
    }
}