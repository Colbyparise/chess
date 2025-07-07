package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public record ChessMove(ChessPosition getStartPosition, ChessPosition getEndPosition,
                        ChessPiece.PieceType getPromotionPiece) {


    /**
     * @return ChessPosition of starting location
     */
    public ChessPosition startPosition() {
        return getStartPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    public ChessPosition endPosition() {
        return getEndPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */

    public ChessPiece.PieceType promotionPiece() {
        return getPromotionPiece;
    }

    @Override
    public String toString() {
        return String.format(
                "ChessMove{startPosition = %s, endPosition=%s, promotionPiece=%s}",
                getStartPosition, getEndPosition, getPromotionPiece
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ChessMove move = (ChessMove) object;
        return Objects.equals(getStartPosition, move.getStartPosition)
                && Objects.equals(getEndPosition, move.getEndPosition)
                && getPromotionPiece == move.getPromotionPiece;
    }
}
