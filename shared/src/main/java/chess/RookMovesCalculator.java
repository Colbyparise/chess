package chess;

import java.util.Set;

public class RookMovesCalculator implements PieceMovesCalculator {

    private static final int[][] ROOK_DIRECTIONS = {
            {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };

    @Override
    public Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from) {
        ChessGame.TeamColor team = board.getColor(from);
        return MoveUtils.generateDirectionalMoves(board, from, ROOK_DIRECTIONS, team);
    }
}
