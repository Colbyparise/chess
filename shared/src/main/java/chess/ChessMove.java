package chess;

import java.util.Objects;

/**
 * Represents moving a chess piece on a chessboard
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public record ChessMove(ChessPosition startPosition, ChessPosition endPosition,
                        ChessPiece.PieceType promotionPiece) {


    /**
     * @return ChessPosition of starting location
     */
    @Override
    public ChessPosition startPosition() {
        return startPosition;
    }

    /**
     * @return ChessPosition of ending location
     */
    @Override
    public ChessPosition endPosition() {
        return endPosition;
    }

    /**
     * Gets the type of piece to promote a pawn to if pawn promotion is part of this
     * chess move
     *
     * @return Type of piece to promote a pawn to, or null if no promotion
     */

    @Override
    public ChessPiece.PieceType promotionPiece() {
        return promotionPiece;
    }

    @Override
    public String toString() {
        return String.format(
                "ChessMove{startPosition = %s, endPosition=%s, promotionPiece=%s}",
                startPosition, endPosition, promotionPiece
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessMove Move = (ChessMove) o;
        return Objects.equals(startPosition, Move.startPosition)
                && Objects.equals(endPosition, Move.endPosition)
                && promotionPiece == Move.promotionPiece;
    }
}
