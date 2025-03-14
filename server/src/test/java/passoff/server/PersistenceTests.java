package passoff.server;

import chess.ChessGame;
import org.junit.jupiter.api.*;
import passoff.exception.TestException;
import passoff.model.*;
import server.Server;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class PersistenceTests {

    private static TestServerFacade serverFacade;
    private static Server server;


    @BeforeAll
    public static void init() {
        startServer();
        serverFacade.clear();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    public static void startServer() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);

        serverFacade = new TestServerFacade("localhost", Integer.toString(port));
    }

    @Test
    @DisplayName("Persistence Test")
    public void persistenceTest() throws TestException {
        var initialRowCount = getDatabaseRows();

        TestUser registerRequest = new TestUser("ExistingUser", "existingUserPassword", "eu@mail.com");

        TestAuthResult regResult = serverFacade.register(registerRequest);
        var auth = regResult.getAuthToken();

        // Create a game
        TestCreateRequest createRequest = new TestCreateRequest("Test Game");
        TestCreateResult createResult = serverFacade.createGame(createRequest, auth);

        // Join the game
        TestJoinRequest joinRequest = new TestJoinRequest(ChessGame.TeamColor.WHITE, createResult.getGameID());
        serverFacade.joinPlayer(joinRequest, auth);

        // Check database row count increased
        Assertions.assertTrue(initialRowCount < getDatabaseRows(), "No new data added to database");

        // Restart the server and test persistence
        stopServer();
        startServer();

        // List games after restart
        TestListResult listResult = serverFacade.listGames(auth);
        Assertions.assertEquals(200, serverFacade.getStatusCode(), "Server response code was not 200 OK");
        Assertions.assertEquals(1, listResult.getGames().length, "Missing game(s) in database after restart");

        // Validate game data after restart
        TestListEntry game1 = listResult.getGames()[0];
        Assertions.assertEquals(game1.getGameID(), createResult.getGameID());
        Assertions.assertEquals(getGameName(createRequest), game1.getGameName(), "Game name changed after restart");
        Assertions.assertEquals(registerRequest.getUsername(), game1.getWhiteUsername(),
                "White player username changed after restart");

    }


    private int getDatabaseRows() {
        int rows = 0;
        try {
            Class<?> clazz = Class.forName("dataAccess.DatabaseManager");
            Method getConnectionMethod = clazz.getDeclaredMethod("getConnection");
            getConnectionMethod.setAccessible(true);

            Object obj = clazz.getDeclaredConstructor().newInstance();
            try (Connection conn = (Connection) getConnectionMethod.invoke(obj)) {
                try (var statement = conn.createStatement()) {
                    for (String table : getTables(conn)) {
                        var sql = "SELECT count(*) FROM " + table;
                        try (var resultSet = statement.executeQuery(sql)) {
                            if (resultSet.next()) {
                                rows += resultSet.getInt(1);
                            }
                        }
                    }
                }

            }
        } catch (Exception ex) {
            Assertions.fail("Unable to load database in order to verify persistence. Are you using dataAccess.DatabaseManager to set your credentials?");
        }

        return rows;
    }

    private String getGameName(TestCreateRequest request) {
        try {
            var field = TestCreateRequest.class.getDeclaredField("gameName");
            field.setAccessible(true);
            return (String) field.get(request);
        } catch (Exception e) {
            throw new RuntimeException("Unable to access gameName field", e);
        }
    }

    private List<String> getTables(Connection conn) throws SQLException {
        String sql = """
                    SELECT table_name
                    FROM information_schema.tables
                    WHERE table_schema = DATABASE();
                """;

        List<String> tableNames = new ArrayList<>();
        try (var preparedStatement = conn.prepareStatement(sql)) {
            try (var resultSet = preparedStatement.executeQuery()) {
                while (resultSet.next()) {
                    tableNames.add(resultSet.getString(1));
                }
            }
        }

        return tableNames;
    }
}