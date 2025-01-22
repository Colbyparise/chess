package chess;

import java.util.Collection;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor teamColor;
    private final PieceType pieceType;
    private final PieceMovesCalculator movesCalculator;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;

        switch (type) {
            case KING:
                this.movesCalculator = new KingMoveCalculation();
                break;
            case QUEEN:
                this.movesCalculator = new QueenMovesCalculator();
                break;
            case BISHOP:
                this.movesCalculator = new BishopMovesCalculator();
                break;
            case KNIGHT:
                this.movesCalculator = new KnightMovesCalculator();
                break;
            case ROOK:
                this.movesCalculator = new RookMovesCalculator();
                break;
            case PAWN:
                this.movesCalculator = new PawnMovesCalculator();
                break;
            default:
                throw new IllegalArgumentException("Unknown piece type: " + type);
        }
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
        return teamColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return movesCalculator.pieceMoves(board, myPosition);
    }
}
