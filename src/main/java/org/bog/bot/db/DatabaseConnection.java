package org.bog.bot.db;

import org.bog.bot.Utils.DatabaseLoginLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);
    public static Connection connect() throws SQLException {

        String[] dbLoginInformation = DatabaseLoginLoader.loadDBloginInfo();

        String jdbcUrl = dbLoginInformation[0];
        String username = dbLoginInformation[1];
        String password = dbLoginInformation[2];

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC driver not found", e);
        }
    }
}