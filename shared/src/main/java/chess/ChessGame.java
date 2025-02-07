package chess;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

public class ChessGame {
    private TeamColor teamColor;
    private ChessBoard chessBoard;
    private boolean endGame;

    public ChessGame() {
        chessBoard = new ChessBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamColor;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamColor = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK;

        public String toString() {
            return this == WHITE ? "white" : "black";
        }
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) {
            return null;
        }
        HashSet<ChessMove> allMoves = (HashSet<ChessMove>) chessBoard.getPiece(startPosition).pieceMoves(chessBoard, startPosition);
        HashSet<ChessMove> validMoves = new HashSet<>(allMoves.size());

        for (ChessMove move : allMoves) {
            ChessPosition target = move.getEndPosition();
            ChessPiece captured = chessBoard.getPiece(target);

            chessBoard.addPiece(startPosition, null);
            chessBoard.addPiece(target, piece);

            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }

            chessBoard.addPiece(target, captured);
            chessBoard.addPiece(startPosition, piece);
        }
        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        boolean teamTurn = getTeamTurn() == chessBoard.getSquareTeam(move.getStartPosition());
        Collection<ChessMove> moves = validMoves(move.getStartPosition());
        if (moves == null) {
            throw new InvalidMoveException("Invalid move");
        }
        boolean isValidMove = moves.contains(move);
        if (isValidMove && teamTurn) {
            ChessPiece movePiece = chessBoard.getPiece(move.getStartPosition());
            if (move.getPromotionPiece() != null) {
                movePiece = new ChessPiece(movePiece.getTeamColor(), move.getPromotionPiece());
            }
            chessBoard.addPiece(move.getStartPosition(), null);
            chessBoard.addPiece(move.getEndPosition(), movePiece);

            setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        } else {
            throw new InvalidMoveException(String.format("Valid move: %b Your Turn: %b", isValidMove, teamTurn));
        }
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition king = null;
        for (int row=1; row <= 8 && king == null; row++) {
            for (int column = 1; column <= 8 && king == null; column++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, column));
                if (piece != null && piece.getTeamColor() == teamColor && piece.getPieceType() == ChessPiece.PieceType.KING) {
                    king = new ChessPosition(row, column);
                }
            }
        }
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, column));
                if (piece == null || piece.getTeamColor() == teamColor) {
                    continue;
                }
                for (ChessMove enemy : piece.pieceMoves(chessBoard, new ChessPosition(row, column))) {
                    if (enemy.getEndPosition().equals(king)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && isInStalemate(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = chessBoard.getPiece(position);
                if (piece != null && teamColor == piece.getTeamColor()) {
                    Collection<ChessMove> moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }


    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }

    public void setGameOver(boolean gameOver) {
        this.endGame = gameOver;
    }

    public boolean getGameOver() {
        return endGame;

    }
    @Override
    public String toString() {
        return "ChessGame{" + "teamTurn=" + teamColor + ", board=" + chessBoard + '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChessGame chessGame = (ChessGame) obj;
        return teamColor == chessGame.teamColor && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, chessBoard);
    }
}