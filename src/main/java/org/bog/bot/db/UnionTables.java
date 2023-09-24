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

public class UnionTables {
    private final Logger logger;
    private TextChannel outputChannel;

    public UnionTables(Logger logger, TextChannel outputChannel) {
        this.logger = logger;
        this.outputChannel = outputChannel;
    }

    public void join(List<TextChannel> filteredTextChannels) {
        List<String> correctTableNames = extractTableNamesFromList(filteredTextChannels);

        if (correctTableNames.isEmpty()) {
            logger.info("No tables to join.");
            return;
        }

        String combinedTableName = createCombinedTable();
        String joinQuery = buildJoinQuery(correctTableNames, combinedTableName);
        insertDataIntoCombinedTable(joinQuery, combinedTableName);

        //dropping old tables to clean up database.
        dropOtherTables(correctTableNames);
    }

    private List<String> extractTableNamesFromList(List<TextChannel> filteredTextChannels) {
        List<String> tableNames = new ArrayList<>();

        for (TextChannel filteredTextChannel : filteredTextChannels) {
            tableNames.add(removeHyphensFromTableName(filteredTextChannel.getName().concat(filteredTextChannel.getId())));
        }
        return tableNames;
    }

    private String createCombinedTable() {
        String tableName = generateCombinedTableName();
        String createTableQuery = generateCreateTableQuery(tableName);

        try {
            executeCreateTableQuery(createTableQuery);
            logger.info("Created new table '" + tableName + "'.");
        } catch (SQLException e) {
            logger.error("Failed to create the new table.", e);
            throw new RuntimeException("Failed to create the new table.", e);
        }

        return tableName;
    }

    private String generateCombinedTableName() {
        return "combinedtable" + outputChannel.getGuild().getName().replaceAll("\\s", "");
    }

    private String generateCreateTableQuery(String tableName) {
        return "CREATE TABLE " + tableName + " (" +
                "id VARCHAR(25) PRIMARY KEY, " +
                "contentraw TEXT NOT NULL, " +
                "author TEXT NOT NULL, " +
                "dateOfMessage TEXT NOT NULL, " +
                "image TEXT, " +
                "jumpurl TEXT NOT NULL" +
                ")";
    }

    private void executeCreateTableQuery(String createTableQuery) throws SQLException {
        try (Connection connection = DatabaseConnection.connect();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableQuery);
        }
    }

    private String buildJoinQuery(List<String> tableNames, String combinedTableName) {
        StringBuilder joinQuery = new StringBuilder("SELECT id, contentraw, author, dateOfMessage, image, jumpurl FROM ");
        joinQuery.append(tableNames.get(0));

        for (int i = 1; i < tableNames.size(); i++) {
            joinQuery.append(" UNION ALL SELECT id, contentraw, author, dateOfMessage, image, jumpurl FROM ")
                    .append(tableNames.get(i));
        }

        return "INSERT INTO " + combinedTableName + " SELECT * FROM (" + joinQuery + ") AS CombinedData";
    }

    private void insertDataIntoCombinedTable(String insertDataQuery, String combinedTableName) {
        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(insertDataQuery)) {

            preparedStatement.executeUpdate();
            logger.info("Inserted data into '" + combinedTableName + "'.");
        } catch (SQLException e) {
            logger.error("Failed to insert data into the new table.", e);
            throw new RuntimeException("Failed to insert data into the new table.", e);
        }
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

}
