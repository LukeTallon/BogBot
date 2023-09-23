package org.bog.bot.stuff;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.POJOs.DiscordQuote;
import org.bog.bot.db.DatabasePopulator;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static org.bog.bot.Utils.Utils.removeHyphensFromTableName;

@Data
public class RandomHistory {

    private Logger logger;
    private final Map<Long, List<Message>> channelMessageHistories = new ConcurrentHashMap<>();
    private DatabasePopulator databasePopulator;
    private MessageFormatter messageFormatter = new MessageFormatter();

    public RandomHistory(Logger logger) {
        this.logger = logger; // Initialize the logger here
        this.databasePopulator = new DatabasePopulator(logger);
    }

    public String getRandomQuote(TextChannel channel) {

        String dbTableName = removeHyphensFromTableName(channel.getName().concat(channel.getId()));

        DiscordQuote randomFromDb = databasePopulator.getRandomMessageFromDB(dbTableName);

        if (randomFromDb != null) {
            logger.info("returning a random from database!");
            return messageFormatter.formatMessageDetails(randomFromDb);
        } else {
            return "No messages in memory, please use '!dbload' to populate BogBot's database";
        }
    }

    public void populateMessages(TextChannel channel, TextChannel outputChannel) {
        if (!channelMessageHistories.containsKey(channel.getIdLong())) {
            Message botLoadingResponse = outputChannel.sendMessage("Loading messages from " + channel.getName() + ", please wait...").complete();
            CompletableFuture<List<Message>> allMessages = retrieveAllMessages(channel);
            acceptCompletableFuture(allMessages, messages -> {
                channelMessageHistories.put(channel.getIdLong(), new ArrayList<>(messages));
                logger.info("finale final size: {}", totalMessageCount(channel));
                botLoadingResponse.editMessage(totalMessageCount(channel) + " messages retrieved from " + channel.getName()).queue();
            });
        }
        databasePopulator.setDatabaseMemoryMap(channelMessageHistories);
    }

    private CompletableFuture<List<Message>> retrieveAllMessages(TextChannel channel) {
        logger.info("Beginning message retrieval in channel: {}", channel.getIdLong());
        logger.info("This may take some time if the channel has a large number of messages.");

        return CompletableFuture.supplyAsync(() -> {
            List<Message> messageList = new ArrayList<>();

            // Retrieve and process messages from MessageHistory
            for (Message message : channel.getIterableHistory()) {
                messageList.add(message);
                if (messageList.size() % 1000 == 0) {
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

    //okay
    private void acceptCompletableFuture(
            CompletableFuture<List<Message>> completableFuture,
            Consumer<List<Message>> callback) {

        completableFuture.thenAcceptAsync(callback);
    }


    //okay
    private int totalMessageCount(TextChannel channel) {
        return channelMessageHistories.get(channel.getIdLong()).size();
    }

}