package org.bog.bot.stuff;

import lombok.Data;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Data
public class RandomHistory {

    private static final Logger logger = LoggerFactory.getLogger(RandomHistory.class);
    private final Map<Long, List<Message>> channelMessageHistories = new HashMap<>();
    private List<Message> messageList;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    public String getRandomQuote(TextChannel channel) {

        if (channelMessageHistories.get(channel.getIdLong()) != null) {
            return quoteRandomMessage(channelMessageHistories.get(channel.getIdLong()));
        }else {
            return "No messages in memory";
        }
    }
    public void populateMessages(TextChannel channel,Integer totalMessages){

        if(!channelMessageHistories.containsKey(channel.getIdLong())) {
            setMessageList(getAllMessages(channel, totalMessages));
            channelMessageHistories.put(channel.getIdLong(), messageList);
        }
    }

    private List<Message> getAllMessages(TextChannel channel,Integer totalMessages) {

        List<Message> histlist = new ArrayList<>();
        while (totalMessages > 0) {
            if (totalMessages == 149000) {
                histlist = channel.getHistory().retrievePast(100).complete();
                totalMessages -= 100;
                //logger.info("histlist size: {}",histlist.size());
            } else {
                String lastMessageId = histlist.get(histlist.size() - 1).getId();
                logger.info("messageID: {}",lastMessageId);
                MessageHistory messageHistory = channel.getHistoryAfter(lastMessageId, 100).complete();
                histlist.addAll(messageHistory.retrievePast(100).complete());
                logger.info("histlist size: {}",histlist.size());
                totalMessages -= 100;
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    logger.warn("Thread interrupted while sleeping", e);
                    Thread.currentThread().interrupt(); // Restore the interrupted status
                }
            }
            if (totalMessages == 0 || totalMessages < 0){
                logger.info("total messages complete.");
            }
        }
        return  histlist;
    }

    private String quoteRandomMessage(List<Message> historicMessages) {
        Random random = new Random();
        int randomIndex = random.nextInt(historicMessages.size());
        Message randomMessage = historicMessages.get(randomIndex);

        String quotedMessageContent = randomMessage.getContentRaw();
        String quotedMessageAuthor = randomMessage.getAuthor().getEffectiveName();
        OffsetDateTime quotedMessageDate = randomMessage.getTimeCreated();
        String messageLink = randomMessage.getJumpUrl();


        StringBuilder quotedMessage = new StringBuilder("> " + quotedMessageContent + "\n"
                + "- Posted by: " + quotedMessageAuthor + "\n"
                + "- Date: " + nicelyFormattedDateTime(quotedMessageDate) + "\n"
                + "Link: " + messageLink);


        for (Message.Attachment attachment : randomMessage.getAttachments()) {
            // Check if the attachment is an image
            if (attachment.isImage()) {
                // Add the image URL to the quoted message
                quotedMessage.append("\n").append(attachment.getUrl());
            }
        }

        return quotedMessage.toString();
    }

    private String nicelyFormattedDateTime(OffsetDateTime originalDateTime){

        String formattedDateTime = originalDateTime.format(formatter);
        return formattedDateTime;
    }

}


//    List<Message> iterableMessages = new ArrayList<>();
//
////        try {
////                iterableMessages = channel.getIterableHistory()
////                .takeAsync(150000)
////                .thenApply(list -> list.stream()
////                .collect(Collectors.toList()))
////                .get();
////                } catch (InterruptedException e) {
////                logger.info("InterruptedException: {},", e);
////                e.printStackTrace();
////                } catch (ExecutionException e) {
////                logger.info("ExecutionException: {},", e);
////                e.printStackTrace();
////                }
////                //iterableMessages.forEach(message -> logger.info(String.valueOf(message)));
////                logger.info("all messages: {}", iterableMessages.size());
////                return iterableMessages;