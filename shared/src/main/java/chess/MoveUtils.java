package chess;

import java.util.HashSet;
import java.util.Set;

public class MoveUtils {

    public static boolean onBoard(int row, int col) {
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    public static boolean isOpponent(ChessBoard board, ChessPosition pos, ChessGame.TeamColor team) {
        ChessPiece target = board.getPiece(pos);
        return target != null && board.getColor(pos) != team;
    }

    public static Set<ChessMove> generateDirectionalMoves(ChessBoard board, ChessPosition from,
                                                          int[][] directions, ChessGame.TeamColor team) {
        Set<ChessMove> legalMoves = new HashSet<>();
        int startRow = from.getRow();
        int startCol = from.getColumn();

        for (int[] d : directions) {
            int row = startRow + d[0];
            int col = startCol + d[1];

            while (onBoard(row, col)) {
                ChessPosition to = new ChessPosition(row, col);
                ChessPiece piece = board.getPiece(to);

                if (piece == null) {
                    legalMoves.add(new ChessMove(from, to, null));
                } else {
                    if (board.getColor(to) != team) {
                        legalMoves.add(new ChessMove(from, to, null));
                    }
                    break;
                }
                row += d[0];
                col += d[1];
            }
        }
        return legalMoves;
    }

    public static Set<ChessMove> generateFixedMoves(ChessBoard board, ChessPosition from,
                                                    int[][] offsets, ChessGame.TeamColor team) {
        Set<ChessMove> legalMoves = new HashSet<>();

        int baseRow = from.getRow();
        int baseCol = from.getColumn();

        for (int[] offset : offsets) {
            int row = baseRow + offset[0];
            int col = baseCol + offset[1];

            if (onBoard(row, col)) {
                ChessPosition to = new ChessPosition(row, col);
                ChessPiece target = board.getPiece(to);
                if (target == null || board.getColor(to) != team) {
                    legalMoves.add(new ChessMove(from, to, null));
                }
            }
        }

        return legalMoves;
    }
}
