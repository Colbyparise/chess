package model;

import chess.ChessGame;

// create record classes and add them to the shared
// module that represent the classes used for the chess application's core data objects
public record GameData(int gameID, String whiteUsername, String blackUsername, String gameName, ChessGame game) {
}
