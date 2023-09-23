package org.bog.bot.stuff;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.POJOs.DiscordQuote;
import org.bog.bot.db.DatabaseConnection;
import org.bog.bot.db.TableCreation;
import org.slf4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Data
public class RandomHistory {

    private final Logger logger;
    private final Map<Long, List<Message>> channelMessageHistories = new ConcurrentHashMap<>();

    private final CharSequence LOADING_MESSAGE = "Loading messages, please wait...";
    private final String messageFile = "AllMessageIds.txt";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    public RandomHistory(Logger logger) {
        this.logger = logger;
    }

    public String getRandomQuote(TextChannel channel) {

        String dbTableName = channel.getName().concat(channel.getId());

        DiscordQuote randomFromDb = getRandomMessageFromDB(dbTableName);

        if (randomFromDb != null) {
            logger.info("returning a random from database!");
            return quoteRandomMessage(randomFromDb);
        }else {
            return "No messages in memory, please use '!dbload' to populate BogBot's database";
        }
    }

    public void populateMessages(TextChannel channel) {
        if (!channelMessageHistories.containsKey(channel.getIdLong())) {
            Message botLoadingResponse = channel.sendMessage(LOADING_MESSAGE).complete();
            CompletableFuture<List<Message>> allMessages = retrieveAllMessages(channel);
            acceptCompletableFuture(allMessages, messages -> {
                channelMessageHistories.put(channel.getIdLong(), new ArrayList<>(messages));
                logger.info("finale final size: {}", totalMessageCount(channel));
                botLoadingResponse.editMessage("Loading finished... " + totalMessageCount(channel) + " messages retrieved.").queue();
            });
        }
    }
    public void populateDB(TextChannel channel){
        if (channelMessageHistories.containsKey(channel.getIdLong())) {
            writeAllMessageIdsToDB(channel, channelMessageHistories.get(channel.getIdLong()));
        } else{
            logger.info("populating the database is not possible, the hashmap does not contain {}'s data.",channel.getIdLong());
        }
    }

    private void acceptCompletableFuture(
            CompletableFuture<List<Message>> completableFuture,
            Consumer<List<Message>> callback) {

        completableFuture.thenAcceptAsync(callback);
    }

    private CompletableFuture<List<Message>> retrieveAllMessages(TextChannel channel) {
        logger.info("Beginning message retrieval in channel: {}", channel.getIdLong());
        logger.info("This may take some time if the channel has a large number of messages.");

        return CompletableFuture.supplyAsync(() -> {
            List<Message> messageList = new ArrayList<>();

            // Retrieve and process messages from MessageHistory
            for (Message message : channel.getIterableHistory()) {
                messageList.add(message);
                if(messageList.size() % 1000 == 0) {
                    logger.info("Messages size: {}", messageList.size());
                }
            }

            logger.info("Messages size: {}", messageList.size());
            return messageList;
        }).exceptionally(e -> {
            logger.error("Error retrieving messages:", e);
            return Collections.emptyList();
        });
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

    private void writeAllMessageIdsToDB(TextChannel channel, List<Message> AllMessagesToDB) {
        String dbTableName = channel.getName().concat(channel.getId());
        logger.info("There are {} messages in the list",AllMessagesToDB.size());
        if (!tableExists(dbTableName)) {
            logger.info("creating {} table.",dbTableName);
            TableCreation tableCreation = new TableCreation(dbTableName);
            tableCreation.TableCreator();
            for (Message message : AllMessagesToDB) {
                DiscordQuote discordQuote = discordQuoteBuilder(message);
                insertMessage(discordQuote,dbTableName);
            }
            logger.info("All Message Ids and raw content written to database");
        }else{
                logger.info("database name already exists so I'll come back to this scenario.");
            }

        }


    private DiscordQuote discordQuoteBuilder(Message message) {
        // Initialize a string to store image URLs
        String imageUrls = null;

        // Loop through the attachments to find image URLs
        for (Message.Attachment attachment : message.getAttachments()) {
            if (attachment.isImage()) {
                // If it's the first image URL, initialize the string
                if (imageUrls == null) {
                    imageUrls = attachment.getUrl() + " \n";
                } else {
                    // If it's not the first, append it with a space and newline
                    imageUrls += attachment.getUrl() + " \n";
                }
            }
        }

        // Build and return the DiscordQuote object
        return DiscordQuote.builder()
                .id(message.getId())
                .contentRaw(message.getContentRaw())
                .author(message.getAuthor().getEffectiveName())
                .dateOfMessage(nicelyFormattedDateTime(message.getTimeCreated()))
                .conditionalImage(imageUrls)
                .jumpUrl(message.getJumpUrl())
                .build();
    }

    public void insertMessage(DiscordQuote discordQuote, String dbTableName) {

        String insertQuery = "INSERT INTO "+dbTableName+"  (id, contentraw, author, dateofmessage, image, jumpurl) VALUES (?, ?, ?, ?, ?, ?)";


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


    private boolean tableExists(String dbTableName){
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

    private String quoteRandomMessage(DiscordQuote discordQuote) {

        StringBuilder quotedMessage = new StringBuilder();

        // Format message content, author, and date
        formatMessageDetails(quotedMessage, discordQuote);

        return quotedMessage.toString();
    }

    private int totalMessageCount(TextChannel channel) {
        return channelMessageHistories.get(channel.getIdLong()).size();
    }

    private void formatMessageDetails(StringBuilder quotedMessage, DiscordQuote discordQuote) {

        String rawMessageContent = boldenText(discordQuote.getContentRaw());
        appendIfNotNullOrEmpty(quotedMessage,rawMessageContent);

        quotedMessage
                .append("- Posted by: ").append(discordQuote.getAuthor()).append("\n")
                .append("- Date: ").append(discordQuote.getDateOfMessage()).append("\n");

        appendIfNotNullOrEmpty(quotedMessage, discordQuote.getConditionalImage());

        quotedMessage.append(discordQuote.getJumpUrl());
    }

    private void appendIfNotNullOrEmpty(StringBuilder builder, String text) {
        if (text != null && !text.isEmpty()) {
            builder.append(text);
        }
    }

    private String nicelyFormattedDateTime(OffsetDateTime originalDateTime) {
        return originalDateTime.format(formatter);
    }

    private String boldenText(String text) {
        if (text != null && !text.isEmpty()) {
            return "**" + text + "**";
        }
        else return null;
    }

}