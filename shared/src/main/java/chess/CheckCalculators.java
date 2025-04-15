package chess;

import calculators.PieceMovesCalculator;

import java.util.Collection;
import java.util.HashSet;

/**
 * Track piece types and check valid moves.
 */
public class CheckCalculators {

    private final PieceMovesCalculator calculator;
    private final HashSet<ChessPiece.PieceType> validPieceTypes;

    public CheckCalculators(PieceMovesCalculator calculator, Collection<ChessPiece.PieceType> pieceTypes) {
        this.calculator = calculator;
        this.validPieceTypes = new HashSet<>(pieceTypes);
    }

    public HashSet<ChessPiece.PieceType> getValidPieceTypes() {
        return validPieceTypes;
    }

    public PieceMovesCalculator getCalculator() {
        return calculator;
    }
}
