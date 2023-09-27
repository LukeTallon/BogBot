package org.bog.bot.RandomMessageFunction.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.bog.bot.Utils.Util.loadDBloginInfo;

public class DatabaseConnection {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConnection.class);

    public static Connection connect() throws SQLException {

        String[] dbLoginInformation = loadDBloginInfo();

        String jdbcUrl = dbLoginInformation[0];
        String username = dbLoginInformation[1];
        String password = dbLoginInformation[2];

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(jdbcUrl, username, password);
        } catch (ClassNotFoundException e) {
            logger.error("The DatabaseConnection class had a ClassNotFoundException", e);
            throw new SQLException("PostgreSQL JDBC driver not found", e);

        }
    }
}