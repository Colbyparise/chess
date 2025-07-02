package chess;

import java.util.Collection;
import java.util.HashSet;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {
    private TeamColor teamColor;
    private ChessBoard chessBoard;
    private boolean gameOver;

    public ChessGame() {
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    public ChessGame copy() {
        ChessGame newGame = new ChessGame();
        newGame.chessBoard = this.chessBoard.copy();
        newGame.teamColor = this.teamColor;
        return newGame;
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
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    //return all legal moves a piece can make, if no piece return null.
    //move is valid if it is a piece move and team king is not in danger.
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        if (piece == null) {
            return null; //return null if no piece
        }
        Collection<ChessMove> legalMove = (HashSet<ChessMove>) chessBoard.getPiece(startPosition).pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        for (ChessMove move : legalMove) {
            ChessGame gameCopy = this.copy();
            try {
                gameCopy.makeMove(move);
                if (!gameCopy.isInCheck(piece.getTeamColor())) {
                    validMoves.add(move);
                }
            } catch (InvalidMoveException e) {

            }
        }
        return validMoves;

    }
    /**
     * Makes a move in a chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    //executes a move, if not legal throw InvalidMoveException
    //move is illegal if not valid for the piece at starting location, or not teams turn
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece piece = chessBoard.getPiece(move.getStartPosition());

        if (piece == null || piece.getTeamColor() != teamColor) {
            throw new InvalidMoveException("No valid piece / not your turn.");
        }

        Collection<ChessMove> validMoves = validMoves(move.getStartPosition());

        if (validMoves == null || !validMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move.");
        }

        ChessPiece movedPiece = (move.getPromotionPiece() != null)
                ? new ChessPiece(teamColor, move.getPromotionPiece())
                : piece;

        chessBoard.addPiece(move.getEndPosition(), movedPiece);
        chessBoard.addPiece(move.getStartPosition(), null);

        teamColor = (teamColor == TeamColor.WHITE) ? TeamColor.BLACK : TeamColor.WHITE;

    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    //returns true if the specified team's King could be captured
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        //looks for the king
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <=8; col++) {
                ChessPiece piece = chessBoard.getPiece(new ChessPosition(row, col));
                if (piece != null && piece.getPieceType() == ChessPiece.PieceType.KING && piece.getTeamColor() == teamColor) {
                    kingPosition = new ChessPosition(row, col);
                    break;
                }
            }
            if (kingPosition != null) {
                break;
            }
        }
        if (kingPosition == null) {
            return false;
        }

        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece opponentPiece = chessBoard.getPiece(position);

                if (opponentPiece != null && opponentPiece.getTeamColor() != teamColor) {
                    var moves = opponentPiece.pieceMoves(chessBoard, position);
                    for (ChessMove move : moves) {
                        if (move.getEndPosition().equals(kingPosition)) {
                            return true;                        }
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
    //returns true if given team has no way to protect their king
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */

    //returns true if team has no legal moves but king is not in check
    public boolean isInStalemate(TeamColor teamColor) {
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
        this.gameOver = gameOver;
    }

    public boolean getGameOver() {
        return gameOver;
    }
}
