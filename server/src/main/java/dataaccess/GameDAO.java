package dataaccess;

import model.GameData;

import java.util.HashSet;

public interface GameDAO {
    void createGame(GameData gameData); //create new game
    GameData getGame(int gameID) throws DataAccessException; //retrieve a specified game with the given gameID
    HashSet<GameData> listGames();//retrieve all games
    boolean gameExists(int gameID);
    void updateGame(GameData gameData) throws DataAccessException; //updates game, used when players join game or move is made
    void clear(); //clears from database
}
