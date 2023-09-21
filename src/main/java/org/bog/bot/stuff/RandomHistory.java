package org.bog.bot.stuff;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.concurrent.atomic.AtomicInteger;

@Data
public class RandomHistory {

    private static final Logger logger = LoggerFactory.getLogger(RandomHistory.class);
    private final CharSequence LOADING_MESSAGE = "Loading messages, please wait...";
    private final String MessageFile = "AllMessageIds.txt";
    private final Map<Long, List<Message>> channelMessageHistories = new ConcurrentHashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    public String getRandomQuote(TextChannel channel) {

        if (channelMessageHistories.get(channel.getIdLong()) != null) {
            return quoteRandomMessage(channelMessageHistories.get(channel.getIdLong()));
        } else {
            return "No messages in memory, please use '!loadMessages' to populate BogBot's sack";
        }
    }

    public void populateMessages(TextChannel channel) {
        if (!channelMessageHistories.containsKey(channel.getIdLong())) {
            CompletableFuture<List<Message>> allMessages = retrieveAllMessages(channel);

            channel.sendMessage(LOADING_MESSAGE).queue(loadingMessage -> allMessages.thenAccept(messages -> {
                channelMessageHistories.put(channel.getIdLong(), messages);
                logger.info("finale final size: {}", totalMessageCount(channel));
                writeAllMessageIdsToFile(messages);
                loadingMessage.editMessage("Loading finished... " + totalMessageCount(channel) + " messages retrieved.").queue();
            }));
        }
    }

    private CompletableFuture<List<Message>> retrieveAllMessages(TextChannel channel) {
        logger.info("Beginning message retrieval in channel: {}", channel.getIdLong());
        logger.info("This may take some time if the channel has a large number of messages.");
        AtomicInteger count = new AtomicInteger();
        List<Message> messages = new ArrayList<>();

        return channel.getIterableHistory()
                .forEachAsync(message -> {
                    aSyncMessagePopulate(message, messages, count);
                    return true; // Continue iteration for all messages
                })
                .thenApply(result -> messages)
                .exceptionally(e -> {
                    logger.error("Error retrieving messages:", e);
                    return Collections.emptyList();
                });
    }

    private void writeAllMessageIdsToFile(List<Message> AllMessageIds) {

        try (PrintWriter writer = new PrintWriter(MessageFile)) {
            for (Message message : AllMessageIds) {
                writer.println(message.getIdLong());
            }
            logger.info("All Message Ids written to" + MessageFile);
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

    private void aSyncMessagePopulate(Message message, List<Message> messages, AtomicInteger count) {
        messages.add(message);
        count.getAndIncrement();
        if (count.get() % 100 == 0) {
            logger.info("Messages size: {}", messages.size());
        }
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