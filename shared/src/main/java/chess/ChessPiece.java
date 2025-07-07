package chess;
import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

// ChessPiece implements rules that define how a piece moves
// independent of other chess rules such as check, stalemate, or checkmate.
// My class uses PieceMovesCalculator

public class ChessPiece {
    private final ChessGame.TeamColor color;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType type) {
        this.color = pieceColor;
        this.type = type;
    }


    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return color;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return type;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type) {
            case KING -> KingMovesCalculator.calculateMoves(board, myPosition);
            case QUEEN -> QueenMovesCalculator.calculateMoves(board, myPosition);
            case ROOK -> RookMovesCalculator.calculateMoves(board, myPosition);
            case BISHOP -> BishopMovesCalculator.calculateMoves(board, myPosition);
            case KNIGHT -> KnightMovesCalculator.calculateMoves(board, myPosition);
            case PAWN -> PawnMovesCalculator.calculateMoves(board, myPosition);
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece other = (ChessPiece) o;
        return color == other.color && type == other.type;
    }
    @Override
    public int hashCode() {
        return 31 * color.hashCode() + type.hashCode();
    }
}

