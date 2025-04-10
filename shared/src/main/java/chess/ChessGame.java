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

    private TeamColor teamTurn;
    private ChessBoard board;

    private boolean gameOver;

    public ChessGame() {
        board = new ChessBoard();
        setTeamTurn(TeamColor.WHITE);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
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
        ChessPiece currPiece = board.getPiece(startPosition);
        if (currPiece == null) {
            return null;
        }
        HashSet<ChessMove> possibleMoves = (HashSet<ChessMove>) board.getPiece(startPosition).pieceMoves(board, startPosition);
        HashSet<ChessMove> validMoves = HashSet.newHashSet(possibleMoves.size());
        for (ChessMove move : possibleMoves) {
            ChessPiece tempPiece = board.getPiece(move.getEndPosition());
            board.addPiece(startPosition, null); //Remove the piece so that it can be moved to a new spot temporarily
            board.addPiece(move.getEndPosition(), currPiece);
            if (!isInCheck(currPiece.getTeamColor())) {
                validMoves.add(move);
            }
            //Reset the board to its original layout
            board.addPiece(move.getEndPosition(), tempPiece);
            board.addPiece(startPosition, currPiece);

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
        boolean isTeamsTurn = getTeamTurn() == board.getSquareTeam(move.getStartPosition());
        Collection<ChessMove> goodMoves = validMoves(move.getStartPosition());
        if (goodMoves == null) {
            throw new InvalidMoveException("No valid moves available");
        }
        boolean isValidMove = goodMoves.contains(move);

        if (isValidMove && isTeamsTurn) {
            ChessPiece pieceToMove = board.getPiece(move.getStartPosition());
            if (move.getPromotionPiece() != null) { //Change piece type if promotion is applied
                pieceToMove = new ChessPiece(pieceToMove.getTeamColor(), move.getPromotionPiece());
            }

            board.addPiece(move.getStartPosition(), null);
            board.addPiece(move.getEndPosition(), pieceToMove);
            setTeamTurn(getTeamTurn() == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE);
        }
        else {
            throw new InvalidMoveException(String.format("Valid move: %b  Your Turn: %b", isValidMove, isTeamsTurn));
        }
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPos = null;
        for (int y = 1; y <= 8 && kingPos == null; y++) { //Find the king
            for (int x = 1; x <= 8  && kingPos == null; x++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(y, x));
                if (currPiece == null) {
                    continue;
                }
                if (currPiece.getTeamColor() == teamColor && currPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    kingPos = new ChessPosition(y, x);
                }
            }
        }

        // See if any enemy piece can attack the king
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPiece currPiece = board.getPiece(new ChessPosition(y, x));
                if (currPiece == null || currPiece.getTeamColor() == teamColor) {
                    continue;
                }
                for (ChessMove enemyMove : currPiece.pieceMoves(board, new ChessPosition(y, x))) {
                    if (enemyMove.getEndPosition().equals(kingPos)) {
                        return true;
                    }
                }
            }
        }

        return false; //A piece wasn't found that can attack the king if it made it this far
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
        for (int y = 1; y <= 8; y++) {
            for (int x = 1; x <= 8; x++) {
                ChessPosition currPosition = new ChessPosition(y, x);
                ChessPiece currPiece = board.getPiece(currPosition);
                Collection<ChessMove> moves;

                if (currPiece != null && teamColor == currPiece.getTeamColor()) {
                    moves = validMoves(currPosition);
                    if (moves != null && !moves.isEmpty()) {
                        return false; //if there's even one valid move, then it's not stalemate
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
        this.board = board;
    }
    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }

    public void setGameOver(boolean gameOver) {
        this.gameOver = gameOver;
    }

    public boolean getGameOver() {
        return gameOver;
    }

    @Override
    public String toString() {
        return "ChessGame{" +
                "teamTurn=" + teamTurn +
                ", board=" + board +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(board, chessGame.board);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, board);
    }
}