package dataaccess.interfaces;


import dataaccess.DataAccessException;
import model.AuthData;
import model.GameData;

import java.util.Collection;

public interface GameDAO {
    Collection<GameData> listGames() throws DataAccessException;

    int createGame(AuthData auth, String gameName) throws DataAccessException;

    GameData getGame(AuthData data, int id) throws DataAccessException;

    void updateGame(GameData data) throws DataAccessException;

    void deleteGame(int id) throws DataAccessException;

    void clear() throws DataAccessException;
}