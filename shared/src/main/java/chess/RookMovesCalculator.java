package chess;

import java.util.HashSet;

public class RookMovesCalculator implements PieceMovesCalculator{

    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();

        int[][] moves = {
                {1, 0},
                {0, 1},
                {-1, 0},
                {0, -1}
        };
        ChessGame.TeamColor team = board.getColor(position);
        return PieceMovesCalculator.directionalMoves(board, position, moves, row, col, team);
    }
}