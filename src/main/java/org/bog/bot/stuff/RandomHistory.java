package org.bog.bot.stuff;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
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

        if (channelMessageHistories.get(channel.getIdLong()) != null) {
            return quoteRandomMessage(channelMessageHistories.get(channel.getIdLong()));
        } else {
            return "No messages in memory, please use '!load' to populate BogBot's sack";
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
                writeAllMessageIdsToFile(channelMessageHistories.get(channel.getIdLong()));
            });
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

    private void writeAllMessageIdsToFile(List<Message> AllMessageIds) {

        try (PrintWriter writer = new PrintWriter(messageFile)) {
            for (Message message : AllMessageIds) {
                writer.println(message.getIdLong());
            }
            logger.info("All Message Ids written to" + messageFile);
        } catch (FileNotFoundException e) {
            logger.error("Error writing All Message Ids contents to file", e);
            throw new RuntimeException(e);
        }
    }

    private String quoteRandomMessage(List<Message> historicMessages) {
        Random random = new Random();
        int randomIndex = random.nextInt(historicMessages.size());
        Message randomMessage = historicMessages.get(randomIndex);

        StringBuilder quotedMessage = new StringBuilder();

        // Format message content, author, and date
        formatMessageDetails(quotedMessage, randomMessage);

        // Check and quote attachments
        for (Message.Attachment attachment : randomMessage.getAttachments()) {
            if (attachment.isImage()) {
                quotedMessage.append(attachment.getUrl()).append("\n");
            }
        }

        // Quote message link
        quotedMessage.append("Link: ").append(randomMessage.getJumpUrl()).append("\n");

        return quotedMessage.toString();
    }

    private int totalMessageCount(TextChannel channel) {
        return channelMessageHistories.get(channel.getIdLong()).size();
    }

    private void formatMessageDetails(StringBuilder builder, Message message) {
        builder.append("> ").append(boldenText(message.getContentRaw())).append("\n")
                .append("- Posted by: ").append(message.getAuthor().getEffectiveName()).append("\n")
                .append("- Date: ").append(nicelyFormattedDateTime(message.getTimeCreated())).append("\n");
    }

    private String nicelyFormattedDateTime(OffsetDateTime originalDateTime) {
        return originalDateTime.format(formatter);
    }

    private String boldenText(String text) {
        return "**" + text + "**";
    }

}