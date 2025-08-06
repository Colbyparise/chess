package chess;

import java.util.Set;

public class KnightMovesCalculator implements PieceMovesCalculator {

    private static final int[][] KNIGHT_OFFSETS = {
            {1, 2}, {2, 1}, {-1, 2}, {-2, 1},
            {-1, -2}, {1, -2}, {2, -1}, {-2, -1}
    };

    @Override
    public Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from) {
        ChessGame.TeamColor team = board.getColor(from);
        return MoveUtils.generateFixedMoves(board, from, KNIGHT_OFFSETS, team);
    }
}
