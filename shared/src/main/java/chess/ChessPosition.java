package chess;
import java.util.Objects;
/**
 * Represents a single square position on a chess board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPosition {
    private final int rowIndex;
    private final int columnIndex;

    public ChessPosition(int rowIndex, int columnIndex) {
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    /**
     * @return which row this position is in
     * 1 codes for the bottom row
     */
    public int getRow() {
        return rowIndex;
    }

    /**
     * @return which column this position is in
     * 1 codes for the left row
     */
    public int getColumn() {
        return columnIndex;
    }
    @Override
    public String toString() {
        return String.format("Position: [Row=%d, Column=%d]", rowIndex, columnIndex);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ChessPosition other) {
            return this.rowIndex == other.rowIndex && this.columnIndex == other.columnIndex;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rowIndex, columnIndex);
    }
}

