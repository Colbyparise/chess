package chess;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import chess.ChessBoard.*;


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
    //return all legal moves a piece can make, if no piece return null.
    //move is valid if it is a piece move and team king is not in danger.
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        ChessPiece piece = chessBoard.getPiece(startPosition);
        int row = startPosition.getRow();
        if (piece == null) {
            return null; //return null if no piece
        }
        Collection<ChessMove> legalMove = (HashSet<ChessMove>) chessBoard.getPiece(startPosition).pieceMoves(chessBoard, startPosition);
        Collection<ChessMove> validMoves = new HashSet<>();

        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (canCastleKingside(piece.getTeamColor())) {
                validMoves.add(new ChessMove(startPosition, new ChessPosition(row, 7), null));
            }
            if (canCastleQueenside(piece.getTeamColor())) {
                validMoves.add(new ChessMove(startPosition, new ChessPosition(row, 3), null));
            }
        }


        for (ChessMove move : legalMove) {
            ChessPiece target = chessBoard.getPiece(move.getEndPosition());

            chessBoard.addPiece(startPosition, null);
            chessBoard.addPiece(move.getEndPosition(), piece);

            if (!isInCheck(piece.getTeamColor())) {
                validMoves.add(move);
            }

            chessBoard.addPiece(move.getEndPosition(), target);
            chessBoard.addPiece(startPosition, piece);
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
        ChessPosition startPosition = move.getStartPosition();

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


        //find if king/rook have moved so that we can castle them
        if (piece.getPieceType() == ChessPiece.PieceType.KING) {
            if (teamColor == TeamColor.WHITE) {
                chessBoard.whiteKingMoved = true;
            }
            else chessBoard.blackKingMoved = true;
        }

        if (piece.getPieceType() == ChessPiece.PieceType.ROOK) {
            if (startPosition.equals(new ChessPosition(1, 1))) {
                chessBoard.whiteQueensideRookMoved = true;
            }
            if (startPosition.equals(new ChessPosition(1, 8))) {
                chessBoard.whiteKingsideRookMoved = true;
            }
            if (startPosition.equals(new ChessPosition(8, 1))) {
                chessBoard.blackQueensideRookMoved = true;
            }
            if (startPosition.equals(new ChessPosition(8, 8))) {
                chessBoard.blackKingsideRookMoved = true;
            }
        }
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
        return isInCheck(teamColor) && cantMove(teamColor);
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
        return !isInCheck(teamColor) && cantMove(teamColor);
    }


    public boolean cantMove(TeamColor teamColor) {
        for (int row = 1; row <= 8; row++) {
            for (int column = 1; column <= 8; column++) {
                ChessPosition position = new ChessPosition(row, column);
                ChessPiece piece = chessBoard.getPiece(position);
                Collection<ChessMove> moves;

                if (piece != null && teamColor == piece.getTeamColor()) {
                    moves = validMoves(position);
                    if (moves != null && !moves.isEmpty()) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean canCastleKingside(TeamColor teamColor) {
        int row = (teamColor == TeamColor.WHITE) ? 1 : 8;
        ChessPosition kingPos = new ChessPosition(row, 5);
        ChessPosition fSquare = new ChessPosition(row, 6);
        ChessPosition gSquare = new ChessPosition(row, 7);
        ChessPosition rookPos = new ChessPosition(row, 8);

        boolean kingMoved = (teamColor == TeamColor.WHITE) ? chessBoard.whiteKingMoved : chessBoard.blackKingMoved;
        boolean rookMoved = (teamColor == TeamColor.WHITE) ? chessBoard.whiteKingsideRookMoved : chessBoard.blackKingsideRookMoved;

        // Must not have moved
        if (kingMoved || rookMoved) return false;

        // Squares between must be empty
        if (chessBoard.getPiece(fSquare) != null || chessBoard.getPiece(gSquare) != null) return false;

        // King cannot castle out of, through, or into check
        if (isInCheck(teamColor)) return false;

        ChessPiece king = chessBoard.getPiece(kingPos);
        chessBoard.addPiece(kingPos, null);

        // simulate passing through f
        chessBoard.addPiece(fSquare, king);
        if (isInCheck(teamColor)) {
            chessBoard.addPiece(fSquare, null);
            chessBoard.addPiece(kingPos, king);
            return false;
        }

        // simulate landing on g
        chessBoard.addPiece(fSquare, null);
        chessBoard.addPiece(gSquare, king);
        boolean safe = !isInCheck(teamColor);
        chessBoard.addPiece(gSquare, null);
        chessBoard.addPiece(kingPos, king);

        return safe;
    }

    public boolean canCastleQueenside(TeamColor teamColor) {
        int row = (teamColor == TeamColor.WHITE) ? 1 : 8;
        ChessPosition kingPos = new ChessPosition(row, 5);
        ChessPosition dSquare = new ChessPosition(row, 4);
        ChessPosition cSquare = new ChessPosition(row, 3);
        ChessPosition bSquare = new ChessPosition(row, 2);
        ChessPosition rookPos = new ChessPosition(row, 1);

        boolean kingMoved = (teamColor == TeamColor.WHITE) ? chessBoard.whiteKingMoved : chessBoard.blackKingMoved;
        boolean rookMoved = (teamColor == TeamColor.WHITE) ? chessBoard.whiteQueensideRookMoved : chessBoard.blackQueensideRookMoved;

        if (kingMoved || rookMoved) return false;

        // Squares between must be empty
        if (chessBoard.getPiece(dSquare) != null || chessBoard.getPiece(cSquare) != null || chessBoard.getPiece(bSquare) != null) return false;

        // King cannot castle out of, through, or into check
        if (isInCheck(teamColor)) return false;

        ChessPiece king = chessBoard.getPiece(kingPos);
        chessBoard.addPiece(kingPos, null);

        // simulate passing through d
        chessBoard.addPiece(dSquare, king);
        if (isInCheck(teamColor)) {
            chessBoard.addPiece(dSquare, null);
            chessBoard.addPiece(kingPos, king);
            return false;
        }

        // simulate landing on c
        chessBoard.addPiece(dSquare, null);
        chessBoard.addPiece(cSquare, king);
        boolean safe = !isInCheck(teamColor);
        chessBoard.addPiece(cSquare, null);
        chessBoard.addPiece(kingPos, king);

        return safe;
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

    @Override
    public String toString() {
         return "ChessGame{" + "teamTurn=" + teamColor + ", board =" + chessBoard + '}';
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) object;
        return teamColor == chessGame.teamColor && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamColor, chessBoard);
    }
}
