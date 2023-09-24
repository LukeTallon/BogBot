package org.bog.bot.MessageDispatch;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.TimerTask;

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

    @Override
    public void run() {

        randomQuoteSender.setDbTableName("combinedtable" + guild.getName().replaceAll("\\s", ""));

        String retrievedMessage = randomQuoteSender.getRandomQuote();

        String outGoingMessage = retrievedMessage.length() < 2000 ? retrievedMessage : MESSAGE_TOO_LONG;

        // Send the random message to the target channel
        outputChannel.sendMessage(outGoingMessage).queue();
    }
}
