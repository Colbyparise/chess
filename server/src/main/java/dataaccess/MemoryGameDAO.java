package dataaccess;
import model.GameData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {

    private final Map<Integer, GameData> games = new HashMap<>();

    @Override
    public void createGame(GameData newGame) throws DataAccessException {
        int id = newGame.gameID();
        if (games.containsKey(id)) {
            throw new DataAccessException("Game ID already exists: " + id);
        }
        games.put(id, newGame);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = games.get(gameID);
        if (game == null) {
            throw new DataAccessException("No game with ID: " + gameID);
        }
        return game;
    }

    @Override
    public Collection<GameData> listGames() {
        return games.values();
    }

    @Override
    public boolean gameExists(int gameID) {
        return games.containsKey(gameID);
    }

    @Override
    public void updateGame(GameData updatedGame) throws DataAccessException {
        int id = updatedGame.gameID();
        if (!games.containsKey(id)) {
            throw new DataAccessException("Cannot update non-existent game with ID: " + id);
        }
        games.put(id, updatedGame);
    }

    @Override
    public void clear() {
        games.clear();
    }
}