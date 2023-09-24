package org.bog.bot.db;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import static org.bog.bot.Utils.Utils.removeHyphensFromTableName;

public class JoinTablesIntoOne {
    private final Logger logger;
    TextChannel outputChannel;

    public JoinTablesIntoOne(Logger logger, TextChannel outputChannel) {
        this.logger = logger;
        this.outputChannel = outputChannel;
    }

    public void join(List<TextChannel> filteredTextChannels) {

        List<String> correctTableNames = extractTableNamesFromList(filteredTextChannels);

        if (correctTableNames.isEmpty()) {
            logger.info("No tables to join.");
            return;
        }

        // Build the SQL query to join tables using UNION ALL
        StringBuilder joinQuery = new StringBuilder("SELECT id, contentraw, author, dateOfMessage, image, jumpurl FROM ");

        joinQuery.append(correctTableNames.get(0));

        // Add UNION ALL clauses for the remaining tables
        for (int i = 1; i < correctTableNames.size(); i++) {
            joinQuery.append(" UNION ALL SELECT id, contentraw, author, dateOfMessage, image, jumpurl FROM ").append(correctTableNames.get(i));
        }

        String createTableQuery = "CREATE TABLE combinedtable (" +
                "id VARCHAR(25) PRIMARY KEY, " +
                "contentraw TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "dateOfMessage TEXT NOT NULL, " +
                "image TEXT, " +
                "jumpurl TEXT NOT NULL" +
                ")";

        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableQuery);
            logger.info("Created new table 'combinedtable'.");
        } catch (SQLException e) {
            logger.error("Failed to create the new table.", e);
            throw new RuntimeException("Failed to create the new table.", e);
        }

        String insertDataQuery = "INSERT INTO CombinedTable SELECT * FROM (" + joinQuery.toString() + ") AS CombinedData";

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(insertDataQuery)) {

            preparedStatement.executeUpdate();
            logger.info("Inserted data into 'combinedtable'.");
        } catch (SQLException e) {
            logger.error("Failed to insert data into the new table.", e);
            throw new RuntimeException("Failed to insert data into the new table.", e);
        }

        dropOtherTables(correctTableNames);
    }

    private void dropOtherTables(List<String> tableNamesToDrop) {

        for (String tableName : tableNamesToDrop) {
            try (Connection connection = DatabaseConnection.connect();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate("DROP TABLE IF EXISTS " + tableName);
                logger.info("Dropped table '{}'.", tableName);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        outputChannel.sendMessage("Database populated and ready!").queue();
    }

    private List<String> extractTableNamesFromList(List<TextChannel> filteredTextChannels) {
        List<String> tableNames = new ArrayList<>();

        for (TextChannel filteredTextChannel : filteredTextChannels) {
            tableNames.add(removeHyphensFromTableName(filteredTextChannel.getName().concat(filteredTextChannel.getId())));
        }
        return tableNames;
    }
}
