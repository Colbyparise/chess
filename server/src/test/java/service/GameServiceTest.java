package service;
import chess.ChessGame;
import dataaccess.*;
import model.AuthData;
import model.GameData;
import org.junit.jupiter.api.*;
import java.util.HashSet;


public class GameServiceTest {
    static GameService gameService;
    static GameDAO gameDAO;
    static AuthDAO authDAO;
    static AuthData authData;

    @BeforeAll
    static void init() {
        gameDAO = new MemoryGameDAO();
        authDAO = new MemoryAuthDAO();
        gameService = new GameService(gameDAO, authDAO);
        authData = new AuthData("Username", "authToken");
        authDAO.addAuth(authData);
    }
    @BeforeEach
    void setup() {
        gameDAO.clear();
    }

    @Test
    @DisplayName("Create Valid Game")
    void createGameTestPositive() throws UnauthorizedException {
        int gameID1 = gameService.createGame(authData.authToken(), "Chess Game 1");
        Assertions.assertTrue(gameDAO.gameExists(gameID1));

        int gameID2 = gameService.createGame(authData.authToken(), "Chess Game 2");
        Assertions.assertNotEquals(gameID1, gameID2);
    }

    @Test
    @DisplayName("Create Invalid Game")
    void createGameTestNegative() {
        Assertions.assertThrows(UnauthorizedException.class, () -> gameService.createGame("badToken", "Invalid Game"));
    }

    @Test
    @DisplayName("Proper List Games")
    void listGamesTestPositive() throws UnauthorizedException {
        int gameID1 = gameService.createGame(authData.authToken(), "Chess Game 1");
        int gameID2 = gameService.createGame(authData.authToken(), "Chess Game 2");
        int gameID3 = gameService.createGame(authData.authToken(), "Chess Game 3");

        HashSet<GameData> expected = new HashSet<>();
        expected.add(new GameData(gameID1, null, null, "Chess Game 1", new ChessGame()));
        expected.add(new GameData(gameID2, null, null, "Chess Game 2", new ChessGame()));
        expected.add(new GameData(gameID3, null, null, "Chess Game 3", new ChessGame()));

        Assertions.assertEquals(expected, gameService.listGames(authData.authToken()));
    }

    @Test
    @DisplayName("Improper List Games")
    void listGamesTestNegative() {
        Assertions.assertThrows(UnauthorizedException.class, () -> gameService.listGames("badToken"));
    }

    @Test
    @DisplayName("Proper Join Game")
    void joinGameTestPositive() throws UnauthorizedException, BadRequestException, DataAccessException {
        int gameID = gameService.createGame(authData.authToken(), "Chess Match");
        gameService.joinGame(authData.authToken(), gameID, "WHITE");

        GameData expectedGameData = new GameData(gameID, authData.username(), null, "Chess Match", new ChessGame());
        Assertions.assertEquals(expectedGameData, gameDAO.getGame(gameID));
    }

    @Test
    @DisplayName("Improper Join Game")
    void joinGameTestNegative() throws UnauthorizedException {
        int gameID = gameService.createGame(authData.authToken(), "Chess Battle");
        Assertions.assertThrows(UnauthorizedException.class, () -> gameService.joinGame("badToken", gameID, "WHITE"));
        Assertions.assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), 11111, "WHITE"));
        Assertions.assertThrows(BadRequestException.class, () -> gameService.joinGame(authData.authToken(), gameID, "INVALID"));
    }

    @Test
    @DisplayName("Proper Clear DB")
    void clearTestPositive() throws UnauthorizedException {
        gameService.createGame(authData.authToken(), "Game to Delete");
        gameService.clear();
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }


    @Test
    @DisplayName("Improper Clear DB")
    void clearTestNegative() throws UnauthorizedException {
        gameService.createGame(authData.authToken(), "Game Before Clear");
        HashSet<GameData> gameListBeforeClear = new HashSet<>(gameDAO.listGames());

        gameService.clear();

        Assertions.assertNotEquals(gameDAO.listGames(), gameListBeforeClear);
        Assertions.assertDoesNotThrow(() -> gameService.clear());  // Ensure clear() doesn't throw exceptions
    }


}