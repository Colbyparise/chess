package chess;
import chess.piecemovement.*;
import java.util.Objects;
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

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.teamColor = pieceColor;
        this.pieceType = type;

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
        return switch (pieceType) {
            case KING -> KingMoveCalculation.getMoves(board, myPosition);
            case QUEEN -> QueenMovesCalculator.getMoves(board, myPosition);
            case BISHOP -> BishopMovesCalculator.getMoves(board, myPosition);
            case ROOK -> RookMovesCalculator.getMoves(board, myPosition);
            case PAWN -> PawnMovesCalculator.getMoves(board, myPosition);
            case KNIGHT -> KnightMovesCalculator.getMoves(board, myPosition);
        };
    }
    @Override
    public String toString() {
        return switch (pieceType) {
            case KING -> teamColor == ChessGame.TeamColor.WHITE ? "K" : "k";
            case QUEEN -> teamColor == ChessGame.TeamColor.WHITE ? "Q" : "q";
            case BISHOP -> teamColor == ChessGame.TeamColor.WHITE ? "B" : "b";
            case KNIGHT -> teamColor == ChessGame.TeamColor.WHITE ? "N" : "n";
            case ROOK -> teamColor == ChessGame.TeamColor.WHITE ? "R" : "r";
            case PAWN -> teamColor == ChessGame.TeamColor.WHITE ? "P" : "p";
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessPiece that = (ChessPiece) obj;
        return teamColor == that.teamColor && pieceType == that.pieceType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, pieceType);
    }
}

