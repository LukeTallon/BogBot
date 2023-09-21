package org.bog.bot.Listeners;

import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bog.bot.stuff.RandomHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyEventListener extends ListenerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(MyEventListener.class);
    private final RandomHistory randomHistory = new RandomHistory();

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            if (!event.getAuthor().isBot()) {
                String message = event.getMessage().getContentRaw();

                // Check if the message was sent in a server
                if (event.isFromGuild()) {
                    String guildId = event.getGuild().getId();

                    if (guildId.equals("431710770737184771")) {
                        resolveRandomQuote(event, message);
                    }

                    if (guildId.equals("690915467778326549")) {
                        resolveRandomQuote(event, message);
                    }
                } else {
                    if (message.equalsIgnoreCase("!hello")) {
                        event.getChannel().sendMessage("Hello, World!").queue();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("An error occurred while processing a message:", e);
        }
    }

    private void resolveRandomQuote(MessageReceivedEvent event, String message) {
        if (message.equalsIgnoreCase("!randy")) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            event.getChannel().sendMessage(randomHistory.getRandomQuote(textChannel)).queue();
        }
        if (message.equalsIgnoreCase("!load")) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            randomHistory.populateMessages(textChannel);
        }
    }
}
