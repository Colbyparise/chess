package chess;
import chess.*;
import java.util.HashSet;

public interface PieceMovesCalculator {
    HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition currentposition, ChessPiece piece);

    //checks to see if piece is in the 8x8 board
    static boolean isOnBoard(ChessPosition position) {
        int row = position.getRow();
        int col = position.getColumn();
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }


    //Moves that are possible using the static relative moves useful for Knight and King
    static HashSet<ChessMove> StaticMoves(ChessPosition currPosition, int[][] relativeMoves, ChessBoard board) {
        HashSet<ChessMove> moves = new HashSet<>(8);

        int row = currPosition.getRow();
        int col = currPosition.getColumn();
        ChessGame.TeamColor color = board.getColor(currPosition);

        for (int[] offset : relativeMoves) {
            ChessPosition newPos = new ChessPosition(row + offset[1], col + offset[0]);
            if (isOnBoard(newPos) && board.getColor(newPos) != color) {
                moves.add(new ChessMove(currPosition, newPos, null));
            }
        }

        return moves;
    }


    static HashSet<ChessMove> DirectionalMoves(ChessBoard board, ChessPosition currentposition, int[][] directions, int row, int col, ChessGame.TeamColor teamColor) {
        return null;
    }
}