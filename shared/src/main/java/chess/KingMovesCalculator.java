package chess;

import java.util.Set;

public class KingMovesCalculator implements PieceMovesCalculator {

    private static final int[][] KING_OFFSETS = {
            {-1, -1}, {-1, 0}, {-1, 1},
            { 0, -1},          { 0, 1},
            { 1, -1}, { 1, 0}, { 1, 1}
    };

    @Override
    public Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from) {
        ChessGame.TeamColor team = board.getColor(from);
        return MoveUtils.generateFixedMoves(board, from, KING_OFFSETS, team);
    }
}
