package chess;

import java.util.Objects;

public class ChessPosition {

    private static final int ASCII_OFFSET = 96; // ASCII code offset for columns ('a' starts at 97)
    private final int rank;
    private final int file;

    public ChessPosition(int row, int col) {
        this.rank = row;
        this.file = col;
    }

    public int getRow() {
        return rank;
    }

    public int getColumn() {
        return file;
    }

    @Override
    public int hashCode() {
        return Objects.hash(rank, file);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }if (!(obj instanceof ChessPosition other)) {
            return false;
        }
        return this.rank == other.rank && this.file == other.file;
    }

    @Override
    public String toString() {
        return "(" + (char) (ASCII_OFFSET + file) + ", " + rank + ")";
    }
}
