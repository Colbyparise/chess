package chess;

import java.util.HashSet;
import java.util.Set;

public class PawnMovesCalculator implements PieceMovesCalculator {

    private static final ChessPiece.PieceType[] PROMOTIONS = {
            ChessPiece.PieceType.QUEEN,
            ChessPiece.PieceType.ROOK,
            ChessPiece.PieceType.BISHOP,
            ChessPiece.PieceType.KNIGHT
    };

    @Override
    public Set<ChessMove> calculateMoves(ChessBoard board, ChessPosition from) {
        Set<ChessMove> moves = new HashSet<>();
        int row = from.getRow();
        int col = from.getColumn();
        ChessGame.TeamColor team = board.getColor(from);
        int forward = (team == ChessGame.TeamColor.WHITE) ? 1 : -1;
        int startRow = (team == ChessGame.TeamColor.WHITE) ? 2 : 7;
        int promoteRow = (team == ChessGame.TeamColor.WHITE) ? 8 : 1;

        // Forward 1
        ChessPosition oneAhead = new ChessPosition(row + forward, col);
        if (MoveUtils.onBoard(oneAhead.getRow(), col) && board.getPiece(oneAhead) == null) {
            addPawnMove(moves, from, oneAhead, oneAhead.getRow() == promoteRow);
            // Forward 2 from start
            if (row == startRow) {
                ChessPosition twoAhead = new ChessPosition(row + 2 * forward, col);
                if (board.getPiece(twoAhead) == null) {
                    moves.add(new ChessMove(from, twoAhead, null));
                }
            }
        }

        // Attacks
        for (int dCol = -1; dCol <= 1; dCol += 2) {
            int targetCol = col + dCol;
            int targetRow = row + forward;
            if (!MoveUtils.onBoard(targetRow, targetCol)) continue;

            ChessPosition diag = new ChessPosition(targetRow, targetCol);
            if (board.getPiece(diag) != null && board.getColor(diag) != team) {
                addPawnMove(moves, from, diag, targetRow == promoteRow);
            }
        }

        return moves;
    }

    private void addPawnMove(Set<ChessMove> moves, ChessPosition from, ChessPosition to, boolean promote) {
        if (promote) {
            for (ChessPiece.PieceType type : PROMOTIONS) {
                moves.add(new ChessMove(from, to, type));
            }
        } else {
            moves.add(new ChessMove(from, to, null));
        }
    }
}
