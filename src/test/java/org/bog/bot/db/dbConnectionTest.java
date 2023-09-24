package org.bog.bot.db;


import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class dbConnectionTest {


    TableCreation tableCreation = new TableCreation("bogbot1");

    @BeforeEach
    private void setUp() {
        tableCreation.TableCreator();
    }

    @AfterEach
    private void afterScenario() {
        tableCreation.TableDropper();
    }

    @Test
    public void testConnectDB() {
        try {
            // Establish a database connection
            Connection connection = DatabaseConnection.connect();

            // Create a SQL query (e.g., select all records from a sample table)
            String sqlQuery = "SELECT * FROM bogbot1"; // Replace with your table name

            // Create a PreparedStatement to execute the query
            PreparedStatement preparedStatement = connection.prepareStatement(sqlQuery);

            // Execute the query and retrieve the result set
            ResultSet resultSet = preparedStatement.executeQuery();

            // Process and display the results (you can customize this part)
            while (resultSet.next()) {
                // Retrieve data from the result set (example: retrieving a column named "column_name")
                String columnNameValue = resultSet.getString("column_name");

                // Print the retrieved value
                System.out.println("Value: " + columnNameValue);
            }

            // Close resources (order matters)
            resultSet.close();
            preparedStatement.close();
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
