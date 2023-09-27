package org.bog.bot.RandomMessageFunction.db;

import org.bog.bot.POJOs.DiscordQuote;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseRetriever {

private final Logger logger;
    public DatabaseRetriever(Logger logger) {
        this.logger = logger;
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
            logger.error("The DatabaseRetriever class had an SQLException", e);
            e.printStackTrace();
        }

        return discordQuote;
    }
}
