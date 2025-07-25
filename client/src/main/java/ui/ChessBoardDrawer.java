package ui;

import chess.ChessBoard;
import chess.ChessPiece;
import chess.ChessPosition;
import chess.ChessGame.TeamColor;

import static ui.EscapeSequences.*;

public class ChessBoardDrawer {
    private final ChessBoard board;

    public ChessBoardDrawer(ChessBoard board) {
        this.board = board;
    }

    public void print(boolean whitePerspective) {
        System.out.print(SET_TEXT_BOLD);
        printColumnHeaders(whitePerspective);

        int[] rowRange = whitePerspective ? range(8, 1, -1) : range(1, 8, 1);
        for (int row : rowRange) {
            printRow(row, whitePerspective);
        }

        printColumnHeaders(whitePerspective);
        System.out.print(RESET_TEXT_BOLD_FAINT);
    }

    private void printColumnHeaders(boolean whitePerspective) {
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + "   ");
        char[] cols = whitePerspective ? "abcdefgh".toCharArray() : "hgfedcba".toCharArray();
        for (char c : cols) {
            System.out.print(" " + c + " ");
        }
        System.out.println("   " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printRow(int row, boolean whitePerspective) {
        System.out.print(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + " " + row + " " + RESET_TEXT_COLOR);

        int[] colRange = whitePerspective ? range(1, 8, 1) : range(8, 1, -1);
        for (int col : colRange) {
            printSquare(row, col);
        }

        System.out.println(SET_BG_COLOR_BLACK + SET_TEXT_COLOR_BLUE + " " + row + " " + RESET_BG_COLOR + RESET_TEXT_COLOR);
    }

    private void printSquare(int row, int col) {
        boolean lightSquare = (row + col) % 2 == 0;
        System.out.print(lightSquare ? SET_BG_COLOR_LIGHT_GREY : SET_BG_COLOR_RED);

        ChessPiece piece = board.getPiece(new ChessPosition(row, col));
        if (piece != null) {
            String color = (piece.getTeamColor() == TeamColor.WHITE) ? SET_TEXT_COLOR_WHITE : SET_TEXT_COLOR_BLACK;
            String symbol = pieceSymbol(piece);
            System.out.print(color + " " + symbol + " ");
        } else {
            System.out.print("   ");
        }
        System.out.print(RESET_TEXT_COLOR);
    }

    private String pieceSymbol(ChessPiece piece) {
        return switch (piece.getPieceType()) {
            case KING -> "K";
            case QUEEN -> "Q";
            case ROOK -> "R";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case PAWN -> "P";
        };
    }

    private int[] range(int start, int end, int step) {
        int size = Math.abs((end - start) / step) + 1;
        int[] r = new int[size];
        for (int i = 0, val = start; i < size; i++, val += step) {
            r[i] = val;
        }
        return r;
    }
}