package ui;

import chess.ChessGame;
import chess.ChessPosition;
import client.ServerFacade;
import model.GameData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;

import static java.lang.System.out;
import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_COLOR;

public class GameplayREPL {

    ServerFacade server;
    BoardPrinter boardPrinter;
    ChessGame game;
    ChessGame.TeamColor color;

    public GameplayREPL(ServerFacade server, ChessGame game, ChessGame.TeamColor color) {
        this.server = server;
        this.boardPrinter = new BoardPrinter(game);
        this.game = game;
        this.color = color;
    }

    public void run() {
        boolean inGame = true;
        out.print(RESET_TEXT_COLOR + RESET_BG_COLOR);
        boardPrinter.printBoard(color, null);
        while (inGame) {
            String[] input = getUserInput();
            switch (input[0]) {
                case "help":
                    printHelpMenu();
                    break;
                case "redraw":
                    boardPrinter.printBoard(color, null);
                    break;
                case "leave":
                    inGame = false;
                    break;
                case "move":
                    if (input.length == 3 && input[1].matches("[a-h][1-8]") && input[2].matches("[a-h][1-8]")) {
                        ChessPosition from = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
                        ChessPosition to = new ChessPosition(input[2].charAt(1) - '0',input[2].charAt(0) - ('a'-1));
                        makeMove(from, to);
                    }
                    else {
                        out.println("Please provide a to and from coordinate (ex: 'c3 d5')");
                        printMakeMove();
                    }
                    break;
                case "resign":
                    confirmResign();
                    break;
                case "highlight":
                    if (input.length == 2 && input[1].matches("[a-h][1-8]")) {
                        ChessPosition position = new ChessPosition(input[1].charAt(1) - '0', input[1].charAt(0) - ('a'-1));
                        out.println(position.toString());
                        boardPrinter.printBoard(color, position);
                    }
                    else {
                        out.println("Please provide a coordinate (ex: 'c3')");
                        printHighlight();
                    }
                    break;
                default:
                    out.println("Command not recognized, please try again");
                    printHelpMenu();
                    break;
            }
        }

        PostloginREPL postloginREPL = new PostloginREPL(server);
        postloginREPL.run();
    }

    private String[] getUserInput() {
        String prompt = color == null ? "OBSERVING" : color == ChessGame.TeamColor.WHITE ? "WHITE" : "BLACK";
        out.printf("\n[%s] >>> ", prompt);
        Scanner scanner = new Scanner(System.in);
        return scanner.nextLine().split(" ");
    }

    private void printHelpMenu() {
        out.println("redraw - redraw the game board");
        out.println("leave - leave the current game");
        printMakeMove();
        out.println("resign - forfeit this game");
        printHighlight();
        out.println("help - show this menu");
    }

    private void printMakeMove() {
        out.println("move <from> <to> - make a move");
    }

    private void printHighlight() {
        out.println("highlight <coordinate> - highlight all legal moves for the given piece");
    }

    private void makeMove(ChessPosition from, ChessPosition to) {

    }

    private void confirmResign() {

    }


}