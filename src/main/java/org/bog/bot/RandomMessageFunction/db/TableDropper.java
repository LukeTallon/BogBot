package org.bog.bot.RandomMessageFunction.db;

import lombok.Data;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


import static org.bog.bot.Utils.Util.loadDBloginInfo;

@Data
public class TableDropper {

    String[] dbLoginInformation = loadDBloginInfo();
    String jdbcUrl = dbLoginInformation[0];
    String username = dbLoginInformation[1];
    String password = dbLoginInformation[2];

    private final Logger logger;
    private String dbTableName;

    public TableDropper(Logger logger, String dbTableName) {
        this.logger = logger;
        this.dbTableName = dbTableName;
    }

    public TableDropper(Logger logger) {
        this.logger = logger;
    }

    public void TableDropper() {
        try {
            // Establish a database connection
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Create a Statement object
            Statement statement = connection.createStatement();

            // Define the SQL DROP TABLE statement
            String dropTableSQL = "DROP TABLE IF EXISTS " + dbTableName;

            // Execute the SQL statement to drop the table
            statement.executeUpdate(dropTableSQL);

            // Close the resources
            statement.close();
            connection.close();
            logger.info("Table " + dbTableName + " dropped successfully.");
            System.out.println("Table " + dbTableName + " dropped successfully.");
        } catch (SQLException e) {
            logger.error("The TableDropper class had an SQLException", e);
            e.printStackTrace();
        }
    }
}
