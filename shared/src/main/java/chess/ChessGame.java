package chess;

import calculators.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class ChessGame {

    private ChessBoard board;
    private TeamColor curPlayer;
    private ChessPiece whiteKing;
    private ChessPiece blackKing;


    private GameState curState;


    private static final CheckCalculators[] CHECK_CALCULATORS = {
            new CheckCalculators(new BishopMovesCalculator(), List.of(
                    ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.BISHOP
            )),
            new CheckCalculators(new RookMovesCalculator(), List.of(
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.ROOK
            )),
            new CheckCalculators(new KnightMovesCalculator(), List.of(
                    ChessPiece.PieceType.KNIGHT
            )),
            new CheckCalculators(new KingMovesCalculator(), List.of(
                    ChessPiece.PieceType.ROOK,
                    ChessPiece.PieceType.KING,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.QUEEN
            )),
            new CheckCalculators(new PawnMovesCalculator(), List.of(
                    ChessPiece.PieceType.PAWN,
                    ChessPiece.PieceType.KING,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.QUEEN
            ))
    };

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();

        curPlayer = TeamColor.WHITE;

        curState = GameState.IN_PROGRESS;

        //Get the kings
        whiteKing = board.getPiece(new ChessPosition(1, 5));
        blackKing = board.getPiece(new ChessPosition(8, 5));

    }

    public TeamColor getTeamTurn() {
        return curPlayer;
    }


    public void setTeamTurn(TeamColor team) {
        curPlayer = team;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return Objects.equals(board, chessGame.board) && curPlayer == chessGame.curPlayer;
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, curPlayer);
    }

    private boolean isValidMove(ChessMove move) {
        var pos = move.getStartPosition();
        var piece = board.getPiece(pos);
        boolean returnVal = true;
        if (piece == null) {
            return false;
        }
        var baseOldPiece = board.getPiece(move.getEndPosition());
        var tempTestPiece = board.movePiece(new ChessMove(move.getStartPosition(), move.getEndPosition(), null));
        if (tempTestPiece.getPieceType() == ChessPiece.PieceType.KING) {
            if (tempTestPiece.getTeamColor() == TeamColor.WHITE) {
                whiteKing = tempTestPiece;
            }
            else {
                blackKing = tempTestPiece;
            }
        }

        if (isInCheck(piece.getTeamColor())) {
            returnVal = false;
        }

        var returnedTestPiece = board.movePiece(new ChessMove(move.getEndPosition(), pos, null));
        if (returnedTestPiece.getPieceType() == ChessPiece.PieceType.KING) {
            if (returnedTestPiece.getTeamColor() == TeamColor.WHITE) {
                whiteKing = returnedTestPiece;
            }
            else {
                blackKing = returnedTestPiece;
            }
        }
        if (baseOldPiece != null) {
            var oldPiece = new ChessPiece(baseOldPiece.getTeamColor(), baseOldPiece.getPieceType());
            board.addPiece(move.getEndPosition(), oldPiece);
        }
        return returnVal;
    }

    public Collection<ChessMove> validMoves(ChessPosition startPosition) {

        var testPiece = board.getPiece(startPosition);
        if (testPiece == null) {
            return null;
        }

        ArrayList<ChessMove> testMoves = new ArrayList<>(testPiece.pieceMoves(board, startPosition));

        for (int i = 0; i < testMoves.size(); i++) {
            if (!isValidMove(testMoves.get(i))) {
                testMoves.remove(i);
                i--;
            }
        }
        return testMoves;
    }

    public void makeMove(ChessMove move) throws InvalidMoveException {

        var endPos = move.getEndPosition();
        var startPos = move.getStartPosition();
        var possibleMoves = validMoves(startPos);
        if (curState != GameState.IN_PROGRESS) {
            throw new InvalidMoveException("Game is Over!");
        }
        else if (possibleMoves == null || !possibleMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move: " + move.toString());
        }
        else if (curPlayer != board.getPiece(move.getStartPosition()).getTeamColor()) {
            throw new InvalidMoveException("Not your turn!");
        }
        else {
            var movedPiece = board.movePiece(move);
            if (movedPiece != null) {
                movedPiece.setHasMoved(true);
                if (movedPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    handleCastleMove(endPos, startPos);
                    if (movedPiece.getTeamColor() == TeamColor.BLACK) {
                        blackKing = movedPiece;
                    }
                    else {
                        whiteKing = movedPiece;
                    }
                }

            }

            curPlayer = curPlayer == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;

            if (isInCheckmate(TeamColor.WHITE)) {
                setCurState(GameState.BLACK_WIN);
            }
            else if (isInCheckmate(TeamColor.BLACK)) {
                setCurState(GameState.WHITE_WIN);
            }
            else if (isInStalemate(curPlayer)) {
                setCurState(GameState.STALEMATE);
            }
        }
    }

    private void handleCastleMove(ChessPosition endPos, ChessPosition startPos) {
        int colMovement = endPos.getColumn() - startPos.getColumn();
        //right castle
        if (colMovement == 2) {
            var rookPos = new ChessPosition(endPos.getRow(), 8);
            board.getPiece(rookPos).setHasMoved(true);
            board.movePiece(new ChessMove(rookPos, new ChessPosition(endPos.getRow(), endPos.getColumn() - 1), null));
        }

        else if (colMovement == -2) {
            var rookPos = new ChessPosition(endPos.getRow(), 1);
            board.getPiece(rookPos).setHasMoved(true);
            board.movePiece(new ChessMove(rookPos, new ChessPosition(endPos.getRow(), endPos.getColumn() + 1), null));
        }
    }



    public boolean isInCheck(TeamColor teamColor) {

        for (var calculator : CHECK_CALCULATORS) {
            ChessPosition kingPos = teamColor == TeamColor.BLACK ? blackKing.getPos() : whiteKing.getPos();
            for (var pos : calculator.getCalculator().pieceMoves(board, kingPos)) {
                var testPiece = board.getPiece(pos.getEndPosition());
                if (testPiece != null && testPiece.getTeamColor() != teamColor) {
                    if (calculator.getValidPieceTypes().contains(testPiece.getPieceType())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean teamCanMove(TeamColor teamColor) {

        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                var testPos = new ChessPosition(i, j);
                var testPiece = board.getPiece(testPos);
                if (testPiece != null && testPiece.getTeamColor() == teamColor) {
                    var move = validMoves(testPos);
                    if (move != null && !move.isEmpty()) {
                        return true;
                    }
                }

            }
        }

        return false;
    }
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && !teamCanMove(teamColor);
    }

    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && !teamCanMove(teamColor);
    }


    public void setBoard(ChessBoard board) {
        this.board = board;
        for (int i = 1; i <= 8; i++) {
            for (int j = 1; j <= 8; j++) {
                var testPiece = board.getPiece(new ChessPosition(i, j));
                if (testPiece != null && testPiece.getPieceType() == ChessPiece.PieceType.KING) {
                    if (testPiece.getTeamColor() == TeamColor.BLACK) {
                        blackKing = testPiece;
                    }
                    else {
                        whiteKing = testPiece;
                    }
                }
            }
        }
    }

    public ChessBoard getBoard() {
        return board;
    }

    public GameState getCurState() {
        return curState;
    }

    public void setCurState(GameState curState) {
        this.curState = curState;
    }

    public enum TeamColor {
        WHITE,
        BLACK
    }

    public enum GameState {
        IN_PROGRESS,
        WHITE_WIN,
        BLACK_WIN,
        STALEMATE
    }

}