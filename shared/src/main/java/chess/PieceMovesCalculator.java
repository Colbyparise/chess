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
        HashSet<ChessMove> moves = new HashSet<>(27);

        for (int[] direction : directions) {
            int i = 1;
            boolean blocked = false;

            while (!blocked) {
                ChessPosition targetPos = new ChessPosition(row + direction[1] * i, col + direction[0] * i);

                if (!isOnBoard(targetPos)) {
                    blocked = true;
                } else if (board.getPiece(targetPos) == null) {
                    moves.add(new ChessMove(currentposition, targetPos, null));
                } else if (board.getColor(targetPos) != teamColor) {
                    moves.add(new ChessMove(currentposition, targetPos, null));
                    blocked = true;
                } else {
                    blocked = true;
                }
                i++;
            }
        }
        return moves;
    }
}