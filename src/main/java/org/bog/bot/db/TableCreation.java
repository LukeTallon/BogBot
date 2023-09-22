package org.bog.bot.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class TableCreation {

    String jdbcUrl = "jdbc:postgresql://localhost:5432/bogbotdb";
    String username = "postgres";
    String password = "bing";
    public void TableCreator() {


        try {
            // Establish a database connection
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Create a Statement object
            Statement statement = connection.createStatement();

            // Define the SQL CREATE TABLE statement
            String createTableSQL = "CREATE TABLE bogbot1 (" +
                    "id serial PRIMARY KEY," +
                    "name VARCHAR(255) NOT NULL" +
                    ")";

            // Execute the SQL statement to create the table
            statement.executeUpdate(createTableSQL);

            // Close the resources
            statement.close();
            connection.close();

            System.out.println("Table 'bogbot1' created successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void TableDropper() {
        try {
            // Establish a database connection
            Connection connection = DriverManager.getConnection(jdbcUrl, username, password);

            // Create a Statement object
            Statement statement = connection.createStatement();

            // Define the SQL DROP TABLE statement
            String dropTableSQL = "DROP TABLE IF EXISTS bogbot1";

            // Execute the SQL statement to drop the table
            statement.executeUpdate(dropTableSQL);

            // Close the resources
            statement.close();
            connection.close();

            System.out.println("Table 'bogbot1' dropped successfully.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
