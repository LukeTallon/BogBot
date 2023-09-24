package org.bog.bot.db;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.POJOs.DiscordQuote;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.bog.bot.Utils.Utils.discordQuoteBuilder;
import static org.bog.bot.Utils.Utils.removeHyphensFromTableName;

@Data
public class DatabasePopulator {
    private final Logger logger;
    private Map<Long, List<Message>> databaseMemoryMap = new ConcurrentHashMap<>();

    public DatabasePopulator(Logger logger) {
        this.logger = logger;
    }

    public void populateDB(TextChannel channel) {

        if (databaseMemoryMap.containsKey(channel.getIdLong())) {
            writeAllMessageIdsToDB(channel, databaseMemoryMap.get(channel.getIdLong()));
        } else {
            logger.info("populating the database is not possible, the hashmap does not contain channel {}'s data.", channel.getName());
        }
    }

    private void writeAllMessageIdsToDB(TextChannel channel, List<Message> AllMessagesInChannel) {

        String dbTableName = removeHyphensFromTableName(channel.getName().concat(channel.getId()));
        logger.info("There are {} messages in the list", AllMessagesInChannel.size());

        if (!tableExists(dbTableName)) {
            logger.info("creating {} table.", dbTableName);
            TableCreation tableCreation = new TableCreation(dbTableName);
            tableCreation.TableCreator();
            for (Message message : AllMessagesInChannel) {
                DiscordQuote discordQuote = discordQuoteBuilder(message);
                insertMessage(discordQuote, dbTableName);
            }
            logger.info("All Message Ids and raw content written to database");
        } else {
            logger.info("database name already exists so I'll come back to this scenario.");
        }
    }

    public DiscordQuote getRandomMessageFromDB(String dbTableName) {
        String selectQuery = "SELECT id, contentraw, author, dateofmessage, image, jumpurl FROM " + dbTableName + " ORDER BY random() LIMIT 1";
        DiscordQuote discordQuote = null;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(selectQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                discordQuote = DiscordQuote.builder()
                        .id(resultSet.getString("id"))
                        .contentRaw(resultSet.getString("contentraw"))
                        .author(resultSet.getString("author"))
                        .dateOfMessage(resultSet.getString("dateofmessage"))
                        .conditionalImage(resultSet.getString("image"))
                        .jumpUrl(resultSet.getString("jumpurl"))
                        .build();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return discordQuote;
    }

    public void insertMessage(DiscordQuote discordQuote, String dbTableName) {

        String insertQuery = "INSERT INTO " + dbTableName + "  (id, contentraw, author, dateofmessage, image, jumpurl) VALUES (?, ?, ?, ?, ?, ?)";


        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)) {
            preparedStatement.setString(1, discordQuote.getId());
            preparedStatement.setString(2, discordQuote.getContentRaw());
            preparedStatement.setString(3, discordQuote.getAuthor());
            preparedStatement.setString(4, discordQuote.getDateOfMessage());
            preparedStatement.setString(5, discordQuote.getConditionalImage());
            preparedStatement.setString(6, discordQuote.getJumpUrl());

            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to insert message into the database. method insertMessage:");
        }
    }


    private boolean tableExists(String dbTableName) {
        String checkQuery = "SELECT COUNT(*) FROM " + dbTableName;

        try (Connection connection = DatabaseConnection.connect();
             PreparedStatement preparedStatement = connection.prepareStatement(checkQuery);
             ResultSet resultSet = preparedStatement.executeQuery()) {

            if (resultSet.next()) {
                int rowCount = resultSet.getInt(1);
                // If rowCount is 0, the table exists but is empty.
                // If rowCount is greater than 0, the table exists and has rows.
                return rowCount == 0 || rowCount > 0;
            } else {
                // If there are no results, the table likely does not exist.
                return false;
            }
        } catch (SQLException e) {
            logger.info("table does not exist.");
            return false;
        }
    }

}
