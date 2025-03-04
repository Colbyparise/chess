package chess.piecemovement;

import chess.*;

import java.util.HashSet;

public class BishopMovesCalculator implements PieceMovesCalculator {


    public static HashSet<ChessMove> getMoves(ChessBoard board, ChessPosition position) {
        int x = position.getColumn();
        int y = position.getRow();

        //possible bishop moves
        int[][] directions = {
                {-1, -1}, //Top-Left
                {-1, 1}, //Top-right
                {1, -1}, //Bot-left
                {1, 1} //Bot-right
        };

        ChessGame.TeamColor team = board.getSquareTeam(position); //get color
        return PieceMovesCalculator.directionalmoves(board, position, directions, y, x, team); //get moves
    }
}

