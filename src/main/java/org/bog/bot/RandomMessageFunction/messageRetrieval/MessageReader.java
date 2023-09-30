package org.bog.bot.RandomMessageFunction.messageRetrieval;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.RandomMessageFunction.db.DatabasePopulator;
import org.bog.bot.RandomMessageFunction.messageDispatch.RandomQuoteShipper;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class MessageReader {

    private final Map<Long, List<Message>> channelMessageHistories = new ConcurrentHashMap<>();
    private final List<CompletableFuture<Void>> populateFutures = new ArrayList<>();
    RandomQuoteShipper randomQuoteShipper;
    DatabasePopulator databasePopulator;
    private Logger logger;

    public MessageReader(Logger logger, DatabasePopulator databasePopulator) {
        this.logger = logger;
        this.databasePopulator = databasePopulator;
    }

    public void populateMessages(TextChannel channel) {
        if (!channelMessageHistories.containsKey(channel.getIdLong())) {
            //Message botLoadingResponse = outputChannel.sendMessage("Loading messages from " + channel.getName() + ", please wait...").complete();
            logger.info("Loading messages from {}, please wait...", channel.getName());
            CompletableFuture<List<Message>> allMessages = retrieveAllMessages(channel);

            // Here we are capturing the CompletableFuture returned by the thenAcceptAsync method.
            CompletableFuture<Void> populateFuture = allMessages.thenAcceptAsync(messages -> {
                channelMessageHistories.put(channel.getIdLong(), new ArrayList<>(messages));
                logger.info("finale final size: {}", totalMessageCount(channel));
                //botLoadingResponse.editMessage(totalMessageCount(channel) + " messages retrieved from " + channel.getName()).queue();
            });

            // We add the CompletableFuture to the populateFutures list in a thread-safe manner
            synchronized (populateFutures) {
                populateFutures.add(populateFuture);
            }
        }

        // Rest of your logic remains unchanged
        databasePopulator.setDatabaseMemoryMap(channelMessageHistories);
    }


    private CompletableFuture<List<Message>> retrieveAllMessages(TextChannel channel) {
        logger.info("Beginning message retrieval in channel: {}", channel.getIdLong());
        logger.info("This may take some time if the channel has a large number of messages.");

        return CompletableFuture.supplyAsync(() -> {
            List<Message> messageList = new ArrayList<>();

            // Retrieve and process messages
            for (Message message : channel.getIterableHistory()) {
                messageList.add(message);
                if (messageList.size() % 1000 == 0) {
                    logger.info(channel + " has retrieved {} messages {}", messageList.size());
                }
            }
            return messageList;
        }).exceptionally(e -> {
            logger.error("Error retrieving messages:", e);
            return Collections.emptyList();
        });
    }

    private int totalMessageCount(TextChannel channel) {
        return channelMessageHistories.get(channel.getIdLong()).size();
    }
}
