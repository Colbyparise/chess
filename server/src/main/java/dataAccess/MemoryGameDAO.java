package dataAccess;
import model.GameData;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class MemoryGameDAO implements GameDAO {

    private final Map<Integer, GameData> db;

    public MemoryGameDAO() {
        db = new HashMap<>();
    }

    @Override
    public HashSet<GameData> listGames() {
        return new HashSet<>(db.values());
    }

    @Override
    public void createGame(GameData game) {
        db.put(game.gameID(), game);
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        GameData game = db.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found, id: " + gameID);
        }
        return game;
    }

    @Override
    public boolean gameExists(int gameID) {
        return db.containsKey(gameID);
    }

    @Override
    public void updateGame(GameData game) {
        db.put(game.gameID(), game);
    }

    @Override
    public void clear() {
        db.clear();
    }
}

