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
import java.util.*;

@Data
public class RandomHistory {

    private static final Logger logger = LoggerFactory.getLogger(RandomHistory.class);
    private final Map<Long, List<Message>> channelMessageHistories = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private final int MAX_MESSAGES = 1000;        //current likely max of our group chat in the near future.
    private int totalMessages;
    private List<Message> messageList;

    public String getRandomQuote(TextChannel channel) {

        if (channelMessageHistories.get(channel.getIdLong()) != null) {
            return quoteRandomMessage(channelMessageHistories.get(channel.getIdLong()));
        }else {
            return "No messages in memory, please use '!loadMessages' to populate BogBot's sack";
        }
    }
    public void populateMessages(TextChannel channel){

        if(!channelMessageHistories.containsKey(channel.getIdLong())) {
            setMessageList(retrieveAllMessages(channel));
            channelMessageHistories.put(channel.getIdLong(), messageList);
        }
    }

    private List<Message> retrieveAllMessages(TextChannel channel) {
        List<Message> histlist = new ArrayList<>();
        logger.info("Beginning message retrieval in channel: {}", channel.getIdLong());
        logger.info("This may take some time if the channel has a large number of messages.");

        channel.getIterableHistory()
                .forEachAsync(message ->{
                    logger.info("histlistsizeinsideforeach: {}",histlist.size());
                    return histlist.add(message);
                }).join();

        setTotalMessages(histlist.size()); // Setting this for bot response
        logger.info("histlist final size: {}", histlist.size());

        // Now, write histlist contents to a file
        writeHistListToFile(histlist);

        return histlist;
    }

    private void writeHistListToFile(List<Message> histlist) {
        try (PrintWriter writer = new PrintWriter("histlist.txt")) {
            for (Message message : histlist) {
                writer.println(message.getIdLong());
            }
            logger.info("histlist contents written to histlist.txt");
        } catch (FileNotFoundException e) {
            logger.error("Error writing histlist contents to file", e);
            throw new RuntimeException(e);
        }
    }


    private String quoteRandomMessage(List<Message> historicMessages) {
        Random random = new Random();
        int randomIndex = random.nextInt(historicMessages.size());
        Message randomMessage = historicMessages.get(randomIndex);

        StringBuilder quotedMessage = new StringBuilder();

        // Quote message content
        quotedMessage.append("> ").append(boldenText(randomMessage.getContentRaw())).append("\n");

        // Quote author
        quotedMessage.append("- Posted by: ").append(randomMessage.getAuthor().getEffectiveName()).append("\n");

        // Quote date
        quotedMessage.append("- Date: ").append(nicelyFormattedDateTime(randomMessage.getTimeCreated())).append("\n");

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

    private String nicelyFormattedDateTime(OffsetDateTime originalDateTime) {
        return originalDateTime.format(formatter);
    }

    private String boldenText(String text) {
        return "**" + text + "**";
    }

}