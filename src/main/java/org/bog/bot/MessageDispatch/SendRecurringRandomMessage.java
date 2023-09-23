package org.bog.bot.MessageDispatch;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Data
public class SendRecurringRandomMessage extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(SendRecurringRandomMessage.class);
    private final String MESSAGE_TOO_LONG = "A random message was selected... However, it was over 2,000 characters, and therefore too long to send it. :(";

    private Guild guild;
    private TextChannel outputChannel;
    private List<TextChannel> booneChannels;
    private RandomQuoteSender randomQuoteSender;

    public SendRecurringRandomMessage(Guild guild, TextChannel outputChannel, RandomQuoteSender randomQuoteSender) {
        this.guild = guild;
        this.outputChannel = outputChannel;
        booneChannels = guild.getTextChannels();
        this.randomQuoteSender = randomQuoteSender;
    }
    private int sizeRandomChannelSelector(List<TextChannel> filteredChannels){
        int size = filteredChannels.size();
        Random random = new Random();

        return random.nextInt(size);
    }

    private List<TextChannel> acceptableTextChannels(List<TextChannel> booneChannels){

        return booneChannels
                .stream()
                .filter(channel -> channel.getIdLong() != outputChannel.getIdLong())
                .collect(Collectors.toList());
    }
    @Override
    public void run() {

        List<TextChannel> acceptableChannels = acceptableTextChannels(booneChannels);

        String retrievedMessage = randomQuoteSender.getRandomQuote(acceptableChannels.get(sizeRandomChannelSelector(acceptableChannels)));

        String outGoingMessage = retrievedMessage.length() < 2000 ? retrievedMessage : MESSAGE_TOO_LONG;

        // Send the random message to the target channel
        outputChannel.sendMessage(outGoingMessage).queue();
    }
}
