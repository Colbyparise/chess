package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class SQLGameDAOtests {

    private SQLGameDAO dao;
    private final AuthData testAuth = new AuthData("testUser", "testToken");

    @BeforeEach
    void setUp() {
        dao = new SQLGameDAO();
        dao.clear();
    }

    @Test
    void createGamePositive() throws DataAccessException {
        GameData game = new GameData(1, null, null, "My Game", new ChessGame());
        dao.createGame(game);

        GameData retrieved = dao.getGame(1);
        assertEquals("My Game", retrieved.gameName());
    }

    @Test
    void createGameNegative_DuplicateID() throws DataAccessException {
        GameData game1 = new GameData(1, null, null, "Game A", new ChessGame());
        GameData game2 = new GameData(1, "white", "black", "Game B", new ChessGame());
        dao.createGame(game1);

        assertThrows(DataAccessException.class, () -> dao.createGame(game2));
    }

    @Test
    void getGamePositive() throws DataAccessException {
        GameData game = new GameData(2, null, null, "GetTest", new ChessGame());
        dao.createGame(game);

        GameData found = dao.getGame(2);
        assertNotNull(found);
    }

    @Test
    void getGameNegative_NotFound() {
        assertThrows(DataAccessException.class, () -> dao.getGame(9999));
    }

    @Test
    void updateGamePositive() throws DataAccessException {
        GameData game = new GameData(3, null, null, "Before Update", new ChessGame());
        dao.createGame(game);

        GameData updated = new GameData(3, "whiteUser", "blackUser", "After Update", new ChessGame());
        dao.updateGame(updated);

        GameData found = dao.getGame(3);
        assertEquals("After Update", found.gameName());
        assertEquals("whiteUser", found.whiteUsername());
    }

    @Test
    void updateGameNegative_NonExistent() {
        GameData game = new GameData(999, "w", "b", "Fake", new ChessGame());
        assertThrows(DataAccessException.class, () -> dao.updateGame(game));
    }

    @Test
    void gameExistsPositive() throws DataAccessException {
        dao.createGame(new GameData(4, null, null, "Exists", new ChessGame()));
        assertTrue(dao.gameExists(4));
    }

    @Test
    void gameExistsNegative() {
        assertFalse(dao.gameExists(9876));
    }

    @Test
    void listGamesPositive() throws DataAccessException {
        dao.createGame(new GameData(5, null, null, "Game1", new ChessGame()));
        dao.createGame(new GameData(6, null, null, "Game2", new ChessGame()));

        var games = dao.listGames();
        assertTrue(games.size() >= 2); // or check for specific IDs
    }

    @Test
    void listGamesNegative_Empty() {
        dao.clear();
        assertTrue(dao.listGames().isEmpty());
    }

    @Test
    void clearPositive() throws DataAccessException {
        dao.createGame(new GameData(7, null, null, "ToClear", new ChessGame()));
        dao.clear();
        assertFalse(dao.gameExists(7));
    }
}





