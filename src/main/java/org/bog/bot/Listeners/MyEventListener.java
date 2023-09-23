package org.bog.bot.Listeners;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bog.bot.botLogic.RandomMessageTask;
import org.bog.bot.stuff.RandomHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Timer;
import java.util.stream.Collectors;

public class MyEventListener extends ListenerAdapter {

    private static Logger logger;
    public final String PERSONAL_DISCORD = "431710770737184771";
    public final String KACHIGGLES = "690915467778326549";
    private final String MESSAGE_TOO_LONG = "A random message was selected... However, it was over 2,000 characters, and therefore too long to send it. :(";
    private RandomHistory randomHistory;

    private final JDA jda;

    public MyEventListener(JDA jda) {
        this.jda = jda;
        this.logger = LoggerFactory.getLogger(MyEventListener.class);
        this.randomHistory = new RandomHistory(logger); // Pass the logger here
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            if (!event.getAuthor().isBot()) {
                String message = event.getMessage().getContentRaw();

                // Check if the message was sent in a server
                if (event.isFromGuild()) {
                    String guildId = event.getGuild().getId();

                    if (isPersonalDiscordChannel(guildId)) {
                        initialBotSetUpPersonalDiscord(event.getGuild(), message);
                        resolveRandomQuote(event, message);
                    }

                    if (isKachigglesDiscordChannel(guildId)) {
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

    private boolean isKachigglesDiscordChannel(String guildId) {
        return guildId.equals(KACHIGGLES);
    }

    private boolean isPersonalDiscordChannel(String guildId) {
        return guildId.equals(PERSONAL_DISCORD);
    }

    private void initialBotSetUpPersonalDiscord(Guild guild, String message) {
        if (message.equalsIgnoreCase("!check")) {
            TextChannel outputChannel = guild.getTextChannelById(1154950416761630751L);
            logHashMapSize(randomHistory);
        }
        if (message.equalsIgnoreCase("!setupMemory")) {
            TextChannel outputChannel = guild.getTextChannelById(1154950416761630751L);
            populateAllRandomMessagesForPersonalChannel(outputChannel);
        }
        if (message.equalsIgnoreCase("!setupDB")) {
            TextChannel outputChannel = guild.getTextChannelById(1154950416761630751L);
            setupPersonalChannelDB();
        }
        if (message.equalsIgnoreCase("!startRecurring")) {
            TextChannel outputChannel = guild.getTextChannelById(1154950416761630751L);
            startRecurringPersonalChannel(outputChannel);
        }
    }

    private void populateAllRandomMessagesForPersonalChannel(TextChannel outputChannel) {

        Guild guild = jda.getGuildById(431710770737184771L);
        List<TextChannel> filteredTextChannels = guild.getTextChannels()
                .stream()
                .filter(channel -> !channel.getName().equals("bogbot"))
                .toList();

        for (TextChannel texty : filteredTextChannels) {
            System.out.println(texty.getName());
            randomHistory.populateMessages(texty, outputChannel);
        }
    }

    private void startRecurringPersonalChannel(TextChannel outputChannel) {
        Timer timer = new Timer();

        // Schedule a task to run every hour (adjust the delay and interval as needed)
        long delay = 0;  // Delay before the first execution (in milliseconds)
        long interval = 30000;  // Interval between executions (every 30 sec for testing)
        Guild guild = jda.getGuildById(431710770737184771L);
        timer.scheduleAtFixedRate(new RandomMessageTask(guild, outputChannel), delay, interval);
    }

    private void logHashMapSize(RandomHistory randomHistory) {
        logger.info("keysets in memory: {}", randomHistory.getChannelMessageHistories().keySet());
    }

    private void setupPersonalChannelDB() {
        Guild guild = jda.getGuildById(431710770737184771L);
        List<TextChannel> filteredTextChannels = guild.getTextChannels()
                .stream()
                .filter(channel -> !channel.getName().equals("bogbot"))
                .toList();


        for (TextChannel texty : filteredTextChannels) {
            randomHistory.getDatabasePopulator().populateDB(texty);
        }
    }

    private void resolveRandomQuote(MessageReceivedEvent event, String message) {
        if (message.equalsIgnoreCase("!randy")) {
            TextChannel textChannel = event.getChannel().asTextChannel();

            String retrievedMessage = randomHistory.getRandomQuote(textChannel);
            String outGoingMessage = retrievedMessage.length() < 2000 ? retrievedMessage : MESSAGE_TOO_LONG;

            event.getChannel().sendMessage(outGoingMessage).queue();
        }
        if (message.equalsIgnoreCase("!load")) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            randomHistory.populateMessages(textChannel, textChannel);
        }
        if (message.equalsIgnoreCase("!dbload")) {
            TextChannel textChannel = event.getChannel().asTextChannel();
            randomHistory.getDatabasePopulator().populateDB(textChannel);
        }
    }
}
