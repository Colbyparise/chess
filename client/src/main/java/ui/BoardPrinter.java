package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static java.lang.System.out;
import static ui.EscapeSequences.*;

public class BoardPrinter {

    private final ChessBoard board;

    public BoardPrinter(ChessBoard board) {
        this.board = board;
    }

    public void printBoard() {
        StringBuilder output = new StringBuilder();

        output.append(SET_TEXT_BOLD);

        boolean reversed = true;
        for (int j = 0; j < 2; j++) {
            output.append(formatColumnLabels(reversed));

            for (int i = 8; i > 0; i--) {
                int row = reversed ? (9 - i) : i;
                output.append(formatRow(row, reversed));
            }

            output.append(formatColumnLabels(reversed));
            if (j < 1) {
                output.append("\n");
            }

            reversed = false;
        }

        output.append(RESET_TEXT_BOLD_FAINT);
        out.println(output);
    }

    private String formatColumnLabels(boolean reversed) {
        String labels = reversed
                ? "    h  g  f  e  d  c  b  a    "
                : "    a  b  c  d  e  f  g  h    ";

        return String.format("%s%s%s%s%s\n",
                SET_BG_COLOR_BLACK,
                SET_TEXT_COLOR_BLUE,
                labels,
                RESET_BG_COLOR,
                RESET_TEXT_COLOR);
    }

    private String formatRow(int row, boolean reversed) {
        StringBuilder output = new StringBuilder();

        output.append(SET_BG_COLOR_BLACK)
                .append(SET_TEXT_COLOR_BLUE)
                .append(String.format(" %d ", row));

        for (int i = 1; i <= 8; i++) {
            int column = reversed ? (9 - i) : i;
            output.append(getSquareColor(row, column));
            output.append(getPieceSymbol(row, column));
        }

        output.append(SET_BG_COLOR_BLACK)
                .append(SET_TEXT_COLOR_BLUE)
                .append(String.format(" %d ", row))
                .append(RESET_BG_COLOR)
                .append(RESET_TEXT_COLOR)
                .append("\n");

        return output.toString();
    }

    private String getSquareColor(int row, int column) {
        boolean isDarkSquare = (row + column) % 2 == 0;
        return isDarkSquare ? SET_BG_COLOR_RED : SET_BG_COLOR_LIGHT_GREY;
    }

    private String getPieceSymbol(int row, int column) {
        ChessPosition position = new ChessPosition(row, column);
        ChessPiece piece = board.getPiece(position);

        if (piece == null) {
            return "   ";
        }

        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE)
                ? SET_TEXT_COLOR_WHITE
                : SET_TEXT_COLOR_BLACK;

        String symbol = switch (piece.getPieceType()) {
            case QUEEN -> " Q ";
            case KING -> " K ";
            case BISHOP -> " B ";
            case KNIGHT -> " N ";
            case ROOK -> " R ";
            case PAWN -> " P ";
        };

        return color + symbol;
    }
}

