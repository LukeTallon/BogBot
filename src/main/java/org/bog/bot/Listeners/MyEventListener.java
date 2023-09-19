package org.bog.bot.Listeners;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bog.bot.stuff.RandomHistory;

public class MyEventListener extends ListenerAdapter {

    RandomHistory randomHistory = new RandomHistory();
    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (!event.getAuthor().isBot()) {
            String message = event.getMessage().getContentRaw();

            // Check if the message was sent in a server
            if (event.isFromGuild()) {
                // Get the server (guild) ID
                String guildId = event.getGuild().getId();

                // You can now implement server-specific logic based on the guildId
                // For example, respond differently in different servers
                if (guildId.equals("431710770737184771")) {
                    if (message.equalsIgnoreCase("!randy")) {
                        TextChannel textChannel = event.getChannel().asTextChannel();
                        event.getChannel().sendMessage(randomHistory.getRandomQuote(textChannel)).queue();
                    }
                }
                if (guildId.equals("690915467778326549")) {
                    if (message.equalsIgnoreCase("!randy")) {
                        TextChannel textChannel = event.getChannel().asTextChannel();
                        event.getChannel().sendMessage(randomHistory.getRandomQuote(textChannel)).queue();
                    }
                    if (message.equalsIgnoreCase("!loadMessages")) {
                        TextChannel textChannel = event.getChannel().asTextChannel();
                        randomHistory.populateMessages(textChannel, 149000);
                        event.getChannel().sendMessage("149,000 messages loaded").queue();
                    }

                }
            } else {
                // This is a private message
                if (message.equalsIgnoreCase("!hello")) {
                    event.getChannel().sendMessage("Hello, World!").queue();
                }
            }
        }
    }
}
