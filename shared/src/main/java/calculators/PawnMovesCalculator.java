package calculators;

import chess.*;

import java.util.Collection;
import java.util.HashSet;


public class PawnMovesCalculator implements PieceMovesCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        var possibleMoves = new HashSet<ChessMove>();
        int col = myPosition.getColumn();
        int row = myPosition.getRow();
        var pawn = board.getPiece(myPosition);

        if (pawn == null) return possibleMoves;

        int direction = pawn.getTeamColor() == ChessGame.TeamColor.BLACK ? -1 : 1;

        ChessPosition oneStepForward = new ChessPosition(row + direction, col);
        boolean firstStepClear = tryAddPawnMove(board, myPosition, oneStepForward, possibleMoves, false);

        if ((row == 2 && direction == 1) || (row == 7 && direction == -1)) {
            ChessPosition twoStepsForward = new ChessPosition(row + 2 * direction, col);
            if (firstStepClear) {
                tryAddPawnMove(board, myPosition, twoStepsForward, possibleMoves, false);
            }
        }

        tryAddPawnMove(board, myPosition, new ChessPosition(row + direction, col + 1), possibleMoves, true);
        tryAddPawnMove(board, myPosition, new ChessPosition(row + direction, col - 1), possibleMoves, true);

        return possibleMoves;
    }

    public boolean tryAddPawnMove(ChessBoard board, ChessPosition start, ChessPosition end,
                                  Collection<ChessMove> moves, boolean isCapture) {
        int col = end.getColumn();
        int row = end.getRow();

        if (row < 1 || row > 8 || col < 1 || col > 8) return false;

        var pawn = board.getPiece(start);
        if (pawn == null) return false;

        var destinationPiece = board.getPiece(end);
        boolean targetOccupied = destinationPiece != null;
        boolean enemyPiece = targetOccupied && destinationPiece.getTeamColor() != pawn.getTeamColor();

        boolean isPromotionRow = (row == 1 || row == 8);
        var candidateMoves = new HashSet<ChessMove>();
        if (isPromotionRow) {
            for (ChessPiece.PieceType type : new ChessPiece.PieceType[]{
                    ChessPiece.PieceType.QUEEN,
                    ChessPiece.PieceType.ROOK,
                    ChessPiece.PieceType.BISHOP,
                    ChessPiece.PieceType.KNIGHT}) {
                candidateMoves.add(new ChessMove(start, end, type));
            }
        } else {
            candidateMoves.add(new ChessMove(start, end, null));
        }

        if (!targetOccupied && !isCapture) {
            moves.addAll(candidateMoves);
            return true;
        }

        if (isCapture && enemyPiece) {
            moves.addAll(candidateMoves);
            return true;
        }

        return false;
    }
}
