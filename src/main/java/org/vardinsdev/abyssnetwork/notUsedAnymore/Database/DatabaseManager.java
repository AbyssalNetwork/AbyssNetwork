package org.vardinsdev.abyssnetwork.notUsedAnymore.Database;

import org.vardinsdev.abyssnetwork.AbyssLogger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {
    private static Connection connection;

    public static void connect(String host, int port, String database, String user, String password) throws SQLException {
        System.out.println("Connecting to: jdbc:mariadb://" + host + ":" + port + "/" + database);
        System.out.println("Password being used: " + password);
        connection = DriverManager.getConnection(
                "jdbc:mariadb://" + host + ":" + port + "/" + database + "?autoReconnect=true",
                user,
                password
        );
        createTables();
        AbyssLogger.success("Database connected.");
    }

    public static void disconnect() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            connection.close();
            AbyssLogger.info("Database disconnected.");
        }
    }

    private static void createTables() throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.executeUpdate(
                    "CREATE TABLE IF NOT EXISTS players (" +
                            "uuid VARCHAR(36) PRIMARY KEY," +
                            "username VARCHAR(16) NOT NULL," +
                            "kills INT DEFAULT 0," +
                            "deaths INT DEFAULT 0," +
                            "team INT DEFAULT -1," +
                            "player_rank VARCHAR(32) DEFAULT 'default'," +
                            "is_opped BOOLEAN DEFAULT FALSE" +
                            ")"
            );
        }
    }

    public static Connection getConnection() {
        return connection;
    }
}