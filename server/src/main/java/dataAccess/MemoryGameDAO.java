package dataAccess;

import users.GameData;

import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {
    HashSet<GameData> db;

    public MemoryGameDAO() {
        db = HashSet.newHashSet(16);
    }

    @Override
    public HashSet<GameData> listGames() {
        return db;
    }

    @Override
    public void createGame(GameData game) {
        db.add(game);
    }

    @Override
    public GameData getDame(int gameID) throws DataAccessException {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return game;
            }
        }
        throw new DataAccessException(gameID + "was not found");
    }

    @Override
    public boolean gameFound(int gameID) {
        for (GameData game : db) {
            if (game.gameID() == gameID) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void updateGame(GameData game) {
        try {
            db.remove(getGAme(game.gameID()));
            db.add(game);
        } catch (DataAccessException exception) {
            db.add(game);
        }
    }

    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }
}