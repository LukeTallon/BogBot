package org.bog.bot.RandomMessageFunction.db;

import lombok.Data;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import static org.bog.bot.Utils.Util.loadDBloginInfo;

@Data
public class TableCreation {


    String[] dbLoginInformation = loadDBloginInfo();
    String jdbcUrl = dbLoginInformation[0];
    String username = dbLoginInformation[1];
    String password = dbLoginInformation[2];

    private final Logger logger;
    private String dbTableName;

    public TableCreation(Logger logger, String dbTableName) {
        this.logger = logger;
        this.dbTableName = dbTableName;
    }

    public void TableCreator() {

        try {
            // Establish a database connection
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Create a Statement object
            Statement statement = connection.createStatement();

            // Define the SQL CREATE TABLE statement
            String createTableSQL = "CREATE TABLE " + dbTableName + " ( " +
                    "Id VARCHAR(25) PRIMARY KEY, " +
                    "CONTENTRAW TEXT NOT NULL, " +
                    "AUTHOR TEXT NOT NULL, " +
                    "DATEOFMESSAGE TEXT NOT NULL, " +
                    "IMAGE TEXT, " +
                    "JUMPURL TEXT NOT NULL " +
                    ")";

            // Execute the SQL statement to create the table
            statement.executeUpdate(createTableSQL);

            // Close the resources
            statement.close();
            statement.close();
            connection.close();

            logger.info("Table " + dbTableName + " created successfully.");
            System.out.println("Table " + dbTableName + " created successfully.");
        } catch (SQLException e) {
            logger.error("The TableCreation class had an SQLException", e);
            e.printStackTrace();
        }
    }
}
