package model;

import chess.ChessGame;

public record Game(int gameID, String whiteUsername, String BlackUsername, String gameName, ChessGame game) {
}
