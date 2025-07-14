package chess;

import java.util.HashSet;

public class PawnMovesCalculator implements PieceMovesCalculator {

    public static HashSet<ChessMove> calculateMoves(ChessBoard board, ChessPosition position) {
        HashSet<ChessMove> moves = new HashSet<>();
        int row = position.getRow();
        int col = position.getColumn();
        ChessGame.TeamColor team = board.getColor(position);
        int moveDirection = team == ChessGame.TeamColor.WHITE ? 1 : -1;
        boolean isPromotionRow = (team == ChessGame.TeamColor.WHITE && row == 7) ||
                (team == ChessGame.TeamColor.BLACK && row == 2);

        ChessPiece.PieceType[] promotionOptions = isPromotionRow
                ? new ChessPiece.PieceType[]{ChessPiece.PieceType.ROOK, ChessPiece.PieceType.KNIGHT,
                ChessPiece.PieceType.QUEEN, ChessPiece.PieceType.BISHOP}
                : new ChessPiece.PieceType[]{null};

        for (ChessPiece.PieceType promotionPiece : promotionOptions) {
            ChessPosition forwardPosition = new ChessPosition(row + moveDirection, col);
            if (isPositionValidAndEmpty(board, forwardPosition)) {
                moves.add(new ChessMove(position, forwardPosition, promotionPiece));
            }

            addAttackMoveIfValid(board, moves, position, row, col, moveDirection, -1, promotionPiece, team);
            addAttackMoveIfValid(board, moves, position, row, col, moveDirection, 1, promotionPiece, team);

            if (isInitialPromotionPawn(row, team)) {
                ChessPosition doubleForwardPosition = new ChessPosition(row + 2 * moveDirection, col);
                if (isPositionValidAndEmpty(board, doubleForwardPosition) && board.getPiece(forwardPosition) == null) {
                    moves.add(new ChessMove(position, doubleForwardPosition, promotionPiece));
                }
            }
        }
        return moves;
    }

    private static boolean isPositionValidAndEmpty(ChessBoard board, ChessPosition position) {
        return PieceMovesCalculator.isOnBoard(position) && board.getPiece(position) == null;
    }

    private static void addAttackMoveIfValid(ChessBoard board, HashSet<ChessMove> moves, ChessPosition currentPosition, int row, int col,
                                             int moveDirection, int columnOffset, ChessPiece.PieceType promotionPiece, ChessGame.TeamColor color) {
        ChessPosition attack = new ChessPosition(row + moveDirection, col + columnOffset);
        if (PieceMovesCalculator.isOnBoard(attack) && board.getPiece(attack) != null && board.getColor(attack) != color) {
            moves.add(new ChessMove(currentPosition, attack, promotionPiece));
        }
    }
    private static boolean isInitialPromotionPawn(int row, ChessGame.TeamColor color) {
        return (color == ChessGame.TeamColor.WHITE && row == 2) ||
                (color == ChessGame.TeamColor.BLACK && row == 7);
    }
}