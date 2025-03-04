package chess.piecemovement;
import chess.*;
import java.util.HashSet;

public interface PieceMovesCalculator {

    static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition currentPos) {
        return null; // This is a placeholder
    }

    static boolean isPositionValid(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return (row >= 1 && row <= 8) && (col >= 1 && col <= 8);
    }

    static HashSet<ChessMove> relativemoves(ChessPosition currentPos,
                                            int[][] relativeMoves, ChessBoard board) {
        HashSet<ChessMove> possibleMoves = new HashSet<>(8);
        int currentColumn = currentPos.getColumn();
        int currentRow = currentPos.getRow();
        ChessGame.TeamColor currentTeam = board.getSquareTeam(currentPos);

        for (int[] offset : relativeMoves) {
            ChessPosition newPos = new ChessPosition(currentRow + offset[1], currentColumn + offset[0]);
            if (isPositionValid(newPos) && board.getSquareTeam(newPos) != currentTeam) {
                possibleMoves.add(new ChessMove(currentPos, newPos, null));
            }
        }
        return possibleMoves;
    }

    static HashSet<ChessMove> directionalmoves(ChessBoard board, ChessPosition currentPos,
                                               int[][] directions, int row, int col, ChessGame.TeamColor currentTeam) {
        HashSet<ChessMove> moves = new HashSet<>(27); // Estimated max moves for sliding pieces

        for (int[] direction : directions) {
            int step = 1;
            boolean isBlocked = false;

            while (!isBlocked) {
                ChessPosition targetPos = new ChessPosition(row + direction[1] * step, col + direction[0] * step);

                if (!isPositionValid(targetPos)) {
                    isBlocked = true;
                } else if (board.getPiece(targetPos) == null) {
                    moves.add(new ChessMove(currentPos, targetPos, null));
                } else if (board.getSquareTeam(targetPos) != currentTeam) {
                    moves.add(new ChessMove(currentPos, targetPos, null));
                    isBlocked = true;
                } else {
                    isBlocked = true;
                }
                step++;
            }
        }
        return moves;
    }
}
