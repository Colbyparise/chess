package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * the signature of the existing methods.
 */
public class ChessBoard {

    private final ChessPiece[][] pieces = new ChessPiece[8][8];

    public ChessBoard() {
        clearBoard();
    }

    public void addPiece(ChessPosition position, ChessPiece piece) {
        pieces[position.getRow() - 1][position.getColumn() - 1] = piece;
        piece.setPos(position);
    }

    public void removePiece(ChessPosition position) {
        pieces[position.getRow() - 1][position.getColumn() - 1] = null;
    }

    public ChessPiece movePiece(ChessMove move) {
        ChessPosition start = move.getStartPosition();
        ChessPiece movedPiece = getPiece(start);

        if (movedPiece != null) {
            ChessPiece.PieceType promotionType = move.getPromotionPiece();
            ChessPiece.PieceType typeToUse = promotionType != null ? promotionType : movedPiece.getPieceType();

            ChessPiece newPiece = new ChessPiece(movedPiece.getTeamColor(), typeToUse);
            newPiece.setHasMoved(movedPiece.getHasMoved());
            addPiece(move.getEndPosition(), newPiece);

            removePiece(start);
            return newPiece;
        }

        return null;
    }

    public ChessPiece getPiece(ChessPosition position) {
        return pieces[position.getRow() - 1][position.getColumn() - 1];
    }

    private void clearBoard() {
        for (int row = 0; row < 8; row++) {
            Arrays.fill(pieces[row], null);
        }
    }

    public void resetBoard() {
        clearBoard();

        // Place white pieces
        addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        for (int i = 1; i <= 8; i++) {
            addPiece(new ChessPosition(2, i), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
        }

        // Place black pieces
        addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        for (int i = 1; i <= 8; i++) {
            addPiece(new ChessPosition(7, i), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        for (int row = 8; row >= 1; row--) {
            for (int col = 1; col <= 8; col++) {
                ChessPiece piece = getPiece(new ChessPosition(row, col));
                if (piece == null) {
                    output.append("_");
                } else {
                    char symbol = switch (piece.getPieceType()) {
                        case KING -> 'K';
                        case QUEEN -> 'Q';
                        case ROOK -> 'R';
                        case BISHOP -> 'B';
                        case KNIGHT -> 'N';
                        case PAWN -> 'P';
                    };
                    output.append(piece.getTeamColor() == ChessGame.TeamColor.WHITE ? symbol : Character.toLowerCase(symbol));
                }
            }
            output.append("\n");
        }
        return output.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChessBoard board)) return false;
        return Arrays.deepEquals(this.pieces, board.pieces);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(pieces);
    }
}
