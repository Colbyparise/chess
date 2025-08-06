package chess;

import java.util.Set;

public class BishopMovesCalculator implements PieceMovesCalculator {

    private static final int[][] DIAGONALS = {
            {-1, -1}, {-1, 1}, {1, -1}, {1, 1}
    };

    @Override
    public Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from) {
        ChessGame.TeamColor team = board.getColor(from);
        return MoveUtils.generateDirectionalMoves(board, from, DIAGONALS, team);
    }
}
