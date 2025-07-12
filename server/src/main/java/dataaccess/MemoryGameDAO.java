package dataaccess;
import model.GameData;
import java.util.HashSet;

public class MemoryGameDAO implements GameDAO {
    HashSet<GameData> db;

    public MemoryGameDAO() {
        db = HashSet.newHashSet(16);

    }

    @Override
    public void createGame(GameData gameData) {
        db.add(gameData);
    }


    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        for (GameData gameData : db) {
            if (gameData.gameID() == gameID) {
                return gameData;
            }
        }
        throw new DataAccessException("Game not found, id: " + gameID);
    }

    @Override
    public HashSet<GameData> listGames() {
        return db;
    }

    @Override
    public void updateGame(GameData gameData) {
        try {
            db.remove(getGame(gameData.gameID()));
            db.add(gameData);
        } catch (DataAccessException exception) {
            db.add(gameData);
        }
    }
    @Override
    public void clear() {
        db = HashSet.newHashSet(16);
    }


}
