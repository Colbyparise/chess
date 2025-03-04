package chess.piecemovement;
import chess.*;
import java.util.HashSet;

public class PawnMovesCalculator implements PieceMovesCalculator {

    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();
        int column = position.getColumn();
        int row = position.getRow();

        ChessGame.TeamColor team = board.getSquareTeam(position);
        int moveDirection = team == ChessGame.TeamColor.WHITE ? 1 : -1;

        boolean isPromotionRow = (team == ChessGame.TeamColor.WHITE && row == 7) ||
                (team == ChessGame.TeamColor.BLACK && row == 2);

        ChessPiece.PieceType[] promotionOptions = isPromotionRow
                ? new ChessPiece.PieceType[]{ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT, ChessPiece.PieceType.BISHOP, ChessPiece.PieceType.QUEEN}
                : new ChessPiece.PieceType[]{null};

        for (ChessPiece.PieceType promotionPiece : promotionOptions) {
            // Forward move
            ChessPosition forwardPosition = new ChessPosition(row + moveDirection, column);
            if (isPositionValidAndEmpty(board, forwardPosition)) {
                moves.add(new ChessMove(position, forwardPosition, promotionPiece));
            }

            // Attack moves
            addAttackMoveIfValid(board, moves, position, row, column, moveDirection, -1, promotionPiece, team); // Left attack
            addAttackMoveIfValid(board, moves, position, row, column, moveDirection, 1, promotionPiece, team);  // Right attack

            // Initial double forward move
            if (isInitialPositionForPawn(row, team)) {
                ChessPosition doubleForwardPosition = new ChessPosition(row + 2 * moveDirection, column);
                if (isPositionValidAndEmpty(board, doubleForwardPosition) && board.getPiece(forwardPosition) == null) {
                    moves.add(new ChessMove(position, doubleForwardPosition, promotionPiece));
                }
            }
        }

        return moves;
    }

    private static boolean isPositionValidAndEmpty(ChessBoard board, ChessPosition position) {
        return PieceMovesCalculator.isPositionValid(position) && board.getPiece(position) == null;
    }

    private static void addAttackMoveIfValid(
            ChessBoard board,
            HashSet<ChessMove> moves,
            ChessPosition currentPosition,
            int row,
            int column,
            int moveDirection,
            int columnOffset,
            ChessPiece.PieceType promotionPiece,
            ChessGame.TeamColor team
    ) {
        ChessPosition attackPosition = new ChessPosition(row + moveDirection, column + columnOffset);
        if (PieceMovesCalculator.isPositionValid(attackPosition) &&
                board.getPiece(attackPosition) != null &&
                board.getSquareTeam(attackPosition) != team) {
            moves.add(new ChessMove(currentPosition, attackPosition, promotionPiece));
        }
    }

    private static boolean isInitialPositionForPawn(int row, ChessGame.TeamColor team) {
        return (team == ChessGame.TeamColor.WHITE && row == 2) ||
                (team == ChessGame.TeamColor.BLACK && row == 7);
    }
}