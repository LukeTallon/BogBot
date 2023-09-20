package org.bog.bot.stuff;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Data
public class RandomHistory {

    private static final Logger logger = LoggerFactory.getLogger(RandomHistory.class);
    private final Map<Long, List<Message>> channelMessageHistories = new HashMap<>();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");
    private final int MAX_MESSAGES = 160000;        //current likely max of our group chat in the near future.
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
            setMessageList(getAllMessages(channel));
            channelMessageHistories.put(channel.getIdLong(), messageList);
        }
    }

    private List<Message> getAllMessages(TextChannel channel) {
        List<Message> histlist = new ArrayList<>();
        logger.info("beginning message retrieval in channel: {}", channel.getIdLong());

        channel.getIterableHistory()
                .takeAsync(MAX_MESSAGES)
                .thenApply(list -> list.stream().collect(Collectors.toList()))
                .thenAccept(messages -> histlist.addAll(messages))
                .join();

        setTotalMessages(histlist.size());      //setting this for bot response
        logger.info("histlist final size: {}", histlist.size());
        return histlist;
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