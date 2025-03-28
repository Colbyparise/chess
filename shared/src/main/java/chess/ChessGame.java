package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;

public class ChessGame {
    private TeamColor teamColor;
    private ChessBoard chessBoard;

    public ChessGame() {
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    public TeamColor getTeamTurn() {
        return teamColor;
    }

    public void setTeamTurn(TeamColor team) {
        teamColor = team;
    }

    public enum TeamColor {
        WHITE, BLACK;
        public String toString() {
            return this == WHITE ? "white" : "black";
        }
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        HashSet<ChessMove> allMoves = new HashSet<>(piece.pieceMoves(chessBoard, startPosition));
        HashSet<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : allMoves) {
            if (isValidMove(startPosition, move)) {
                validMoves.add(move);
            }
        }
        return validMoves;
    }

    private boolean isValidMove(ChessPosition start, ChessMove move) {
        ChessPiece piece = chessBoard.getPiece(start);
        ChessPiece target = chessBoard.getPiece(move.getEndPosition());

        chessBoard.addPiece(start, null);
        chessBoard.addPiece(move.getEndPosition(), piece);
        boolean valid = !isInCheck(piece.getTeamColor());

        chessBoard.addPiece(move.getEndPosition(), target);
        chessBoard.addPiece(start, piece);

        return valid;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {
        if (getTeamTurn() != chessBoard.getSquareTeam(move.getStartPosition())) {
            throw new InvalidMoveException("Not your turn");
        }
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        if (moves == null || !moves.contains(move)) {
            throw new InvalidMoveException("Invalid move");
        }

        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());
        if (move.getPromotionPiece() != null) {
            piece = new ChessPiece(piece.getTeamColor(), move.getPromotionPiece());
        }

        chessBoard.addPiece(move.getStartPosition(), null);
        chessBoard.addPiece(move.getEndPosition(), piece);

        setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
    }

    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition king = findKingPosition(teamColor);
        if (king == null) {
            return false;
        }
        return isPositionUnderAttack(king, teamColor);
    }

    private ChessPosition findKingPosition(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, column));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    return new ChessPosition(row, column);
                }
            }
        }
        return null;
    }

    private boolean isPositionUnderAttack(ChessPosition position, TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, column));
                if (piece != null && piece.getTeamColor() != teamColor) {
                    for (ChessMove move : piece.pieceMoves(chessBoard, new ChessPosition(row, column))) {
                        if (move.getEndPosition().equals(position)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && noValidMoves(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && noValidMoves(teamColor);
    }

    private boolean noValidMoves(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = chessBoard.getPiece(position);
                if (piece != null && piece.getTeamColor() == teamColor) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public void setBoard(ChessBoard board) {
        this.chessBoard = board;
    }

    public ChessBoard getBoard() {
        return chessBoard;
    }


    @Override
    public String toString() {
        return "ChessGame{" + "teamTurn=" + teamColor + ", board=" + chessBoard + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) obj;
        return teamColor == chessGame.teamColor && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, chessBoard);
    }
}