package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */

//This class stores all the uncaptured pieces in a Game.
//It needs to support adding and removing pieces for testing,
//as well as a resetBoard() method that sets the standard Chess starting configuration.

public class ChessBoard {
    private ChessPiece[][] squares = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        squares[position.getRow()][position.getColumn()] = piece;
    }
    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return squares[position.getRow()][position.getColumn()];
    }

    //Gets the color of the piece at a certain position

    public ChessGame.TeamColor getColor(ChessPosition position) {
        ChessPiece piece = getPiece(position);
        return (piece != null) ? piece.getTeamColor() : null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        //Empty Board
        squares = new ChessPiece[8][8];

        //lets us do white/black instead of ChessGame.TeamColor.Black everytime
        ChessGame.TeamColor black = ChessGame.TeamColor.BLACK;
        ChessGame.TeamColor white = ChessGame.TeamColor.WHITE;

        //adds the black pieces
        for (int col = 0; col < 8; col++) {
            squares[1][col] = new ChessPiece(black, ChessPiece.PieceType.PAWN);
        }
        squares[0][0] = new ChessPiece(black, ChessPiece.PieceType.ROOK);
        squares[0][1] = new ChessPiece(black, ChessPiece.PieceType.KNIGHT);
        squares[0][2] = new ChessPiece(black, ChessPiece.PieceType.BISHOP);
        squares[0][3] = new ChessPiece(black, ChessPiece.PieceType.QUEEN);
        squares[0][4] = new ChessPiece(black, ChessPiece.PieceType.KING);
        squares[0][5] = new ChessPiece(black, ChessPiece.PieceType.BISHOP);
        squares[0][6] = new ChessPiece(black, ChessPiece.PieceType.KNIGHT);
        squares[0][7] = new ChessPiece(black, ChessPiece.PieceType.ROOK);

        //adds the white pieces
        for (int col = 0; col < 8; col++) {
            squares[6][col] = new ChessPiece(white, ChessPiece.PieceType.PAWN);
        }
        squares[7][0] = new ChessPiece(white, ChessPiece.PieceType.ROOK);
        squares[7][1] = new ChessPiece(white, ChessPiece.PieceType.KNIGHT);
        squares[7][2] = new ChessPiece(white, ChessPiece.PieceType.BISHOP);
        squares[7][3] = new ChessPiece(white, ChessPiece.PieceType.QUEEN);
        squares[7][4] = new ChessPiece(white, ChessPiece.PieceType.KING);
        squares[7][5] = new ChessPiece(white, ChessPiece.PieceType.BISHOP);
        squares[7][6] = new ChessPiece(white, ChessPiece.PieceType.KNIGHT);
        squares[7][7] = new ChessPiece(white, ChessPiece.PieceType.ROOK);
    }
}
