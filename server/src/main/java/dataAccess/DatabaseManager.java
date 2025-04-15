package dataaccess;

import java.sql.*;
import java.util.Properties;

public class DatabaseManager {

    private static final String DATABASE_NAME;
    private static final String USER;
    private static final String PASSWORD;
    private static final String CONNECTION_URL;

    public static final String[] TABLES = {"auth", "games", "users"};

    static {
        try (var propStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("db.properties")) {
            if (propStream == null) {
                throw new RuntimeException("Failed to load db.properties");
            }

            Properties properties = new Properties();
            properties.load(propStream);

            DATABASE_NAME = properties.getProperty("db.name");
            USER = properties.getProperty("db.user");
            PASSWORD = properties.getProperty("db.password");

            String host = properties.getProperty("db.host");
            int port = Integer.parseInt(properties.getProperty("db.port"));
            CONNECTION_URL = String.format("jdbc:mysql://%s:%d", host, port);

        } catch (Exception e) {
            throw new RuntimeException("Error initializing database configuration: " + e.getMessage());
        }
    }

    public static void createDatabase() throws DataAccessException {
        try (Connection connection = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD)) {

            executeStatement(connection, "CREATE DATABASE IF NOT EXISTS " + DATABASE_NAME);
            connection.setCatalog(DATABASE_NAME);

            String createGamesTable = """
                    CREATE TABLE IF NOT EXISTS games (
                        id INT NOT NULL AUTO_INCREMENT,
                        whiteUsername VARCHAR(255),
                        blackUsername VARCHAR(255),
                        gameName VARCHAR(255) NOT NULL,
                        chessGame LONGTEXT NOT NULL,
                        PRIMARY KEY (id)
                    );
                    """;

            String createAuthTable = """
                    CREATE TABLE IF NOT EXISTS auth (
                        authToken VARCHAR(255) NOT NULL,
                        username VARCHAR(255) NOT NULL,
                        PRIMARY KEY (authToken)
                    );
                    """;

            String createUsersTable = """
                    CREATE TABLE IF NOT EXISTS users (
                        username VARCHAR(255) NOT NULL,
                        password VARCHAR(255) NOT NULL,
                        email VARCHAR(255) NOT NULL,
                        PRIMARY KEY (username)
                    );
                    """;

            executeStatement(connection, createGamesTable);
            executeStatement(connection, createAuthTable);
            executeStatement(connection, createUsersTable);

        } catch (SQLException e) {
            throw new DataAccessException("Database creation failed: " + e.getMessage());
        }
    }

    public static Connection getConnection() throws DataAccessException {
        try {
            Connection connection = DriverManager.getConnection(CONNECTION_URL, USER, PASSWORD);
            connection.setCatalog(DATABASE_NAME);
            return connection;
        } catch (SQLException e) {
            throw new DataAccessException("Unable to connect to the database: " + e.getMessage());
        }
    }


    public static void reset() throws DataAccessException {
        try (Connection connection = getConnection()) {
            for (String table : TABLES) {
                String truncateQuery = "TRUNCATE TABLE " + table + ";";
                try (PreparedStatement stmt = connection.prepareStatement(truncateQuery)) {
                    stmt.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to reset tables: " + e.getMessage());
        }
    }


    private static void executeStatement(Connection connection, String sql) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.executeUpdate();
        }
    }

    public enum TableName {
        Auth,
        Games,
        Users
    }
}
