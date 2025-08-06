package chess;

import java.util.Set;

public class QueenMovesCalculator implements PieceMovesCalculator {

    private static final int[][] QUEEN_DIRECTIONS = {
            {-1, -1}, {-1, 0}, {-1, 1}, { 0, -1}, { 0, 1}, { 1, -1}, { 1, 0}, { 1, 1}
    };

    @Override
    public Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from) {
        ChessGame.TeamColor team = board.getColor(from);
        return MoveUtils.generateDirectionalMoves(board, from, QUEEN_DIRECTIONS, team);
    }
}
