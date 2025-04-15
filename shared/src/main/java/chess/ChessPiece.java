package chess;

import java.util.Collection;
import java.util.Objects;

import calculators.*;

 public class ChessPiece {

    private final ChessGame.TeamColor team;
    private final PieceType kind;
    private ChessPosition currentPosition;
    private boolean movedBefore;

    public ChessPiece(ChessGame.TeamColor color, PieceType pieceKind) {
        this.team = color;
        this.kind = pieceKind;
        this.currentPosition = null;
        this.movedBefore = false;
    }


    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    public ChessGame.TeamColor getTeamColor() {
        return team;
    }

    public PieceType getPieceType() {
        return kind;
    }

    public ChessPosition getPos() {
        return currentPosition;
    }

    public void setPos(ChessPosition pos) {
        this.currentPosition = pos;
    }

    public boolean getHasMoved() {
        return movedBefore;
    }


    public void setHasMoved(boolean moved) {
        this.movedBefore = moved;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(team).append(" ").append(kind);
        if (currentPosition != null) {
            sb.append(" @ ").append(currentPosition);
        } else {
            sb.append(" (unplaced)");
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ChessPiece other)) return false;
        return team == other.team &&
                kind == other.kind &&
                Objects.equals(currentPosition, other.currentPosition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(team, kind, currentPosition);
    }

    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (kind) {
            case BISHOP -> new BishopMovesCalculator().pieceMoves(board, myPosition);
            case ROOK -> new RookMovesCalculator().pieceMoves(board, myPosition);
            case QUEEN -> new QueenMovesCalculator().pieceMoves(board, myPosition);
            case KNIGHT -> new KnightMovesCalculator().pieceMoves(board, myPosition);
            case PAWN -> new PawnMovesCalculator().pieceMoves(board, myPosition);
            case KING -> new KingMovesCalculator().pieceMoves(board, myPosition);
        };
    }
}
