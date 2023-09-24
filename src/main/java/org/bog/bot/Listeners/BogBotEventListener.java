package org.bog.bot.Listeners;

import lombok.Data;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bog.bot.MessageDispatch.RandomQuoteSender;
import org.bog.bot.MessageDispatch.SendRecurringRandomMessage;
import org.bog.bot.MessageRetrieval.MessageReader;
import org.bog.bot.db.DatabasePopulator;
import org.bog.bot.db.UnionTables;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;

@Data
public class BogBotEventListener extends ListenerAdapter {

    private static Logger logger;
    private final String MESSAGE_TOO_LONG = "A random message was selected... However, it was over 2,000 characters, and therefore too long to send it. :(";

    private TextChannel outputChannelField;
    private RandomQuoteSender randomQuoteSender;
    private MessageReader messageReader;
    private DatabasePopulator databasePopulator;
    private JDA jda;
    private List<Guild> guilds;

    public BogBotEventListener(JDA jda, List<Guild> guilds) {
        this.jda = jda;
        this.guilds = guilds;
        logger = LoggerFactory.getLogger(BogBotEventListener.class);
        this.databasePopulator = new DatabasePopulator(logger);
        this.randomQuoteSender = new RandomQuoteSender(logger, databasePopulator);
        this.messageReader = new MessageReader(logger, databasePopulator);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        try {
            if (!event.getAuthor().isBot()) {
                String message = event.getMessage().getContentRaw();

                // Check if the message was sent in a server
                if (event.isFromGuild()) {

                    Guild guild = event.getGuild();
                    String guildId = event.getGuild().getId();
                    Map<String, TextChannel> bogBotsHomes = bogBotsChannels(guilds);

                    if (bogBotsHomes.containsKey(guildId)) {
                        TextChannel outputChannel = bogBotsHomes.get(guildId);
                        initializeBogBot(guild, outputChannel, message);
                    }
                }

            }
        } catch (Exception e) {
            logger.error("An error occurred while processing a message:", e);
        }
    }

    private Map<String, TextChannel> bogBotsChannels(List<Guild> guilds) {
        Map<String, TextChannel> resultMap = new HashMap<>();

        for (Guild guild : guilds) {
            Optional<TextChannel> textChannelOptional = guild.getTextChannels()
                    .stream()
                    .filter(channel -> channel.getName().equals("bogbot"))
                    .findFirst();

            textChannelOptional.ifPresent(textChannel -> resultMap.put(guild.getId(), textChannel));
        }

        return resultMap;
    }

    private void initializeBogBot(Guild guild, TextChannel outputChannel, String message) {
        if (message.equalsIgnoreCase("!check")) {
            logHashMapSize(randomQuoteSender);
        }
        if (message.equalsIgnoreCase("!sm")) {
            readMessagesInGuild(guild, outputChannel);
        }
        if (message.equalsIgnoreCase("!sdb")) {
            writeAllMessagesToDB(guild);
        }
        if (message.equalsIgnoreCase("!start")) {
            startSendingRecurringRandomMessage(guild, outputChannel);
        }
    }

    private void readMessagesInGuild(Guild guild, TextChannel outputChannel) {

        List<TextChannel> filteredTextChannels = guild.getTextChannels()
                .stream()
                .filter(channel -> !channel.getName().equals("bogbot"))
                .toList();

        for (TextChannel textChannel : filteredTextChannels) {
            System.out.println(textChannel.getName());
            messageReader.populateMessages(textChannel, outputChannel);
        }
    }

    private void startSendingRecurringRandomMessage(Guild guild, TextChannel outputChannel) {
        Timer timer = new Timer();

        // Schedule a task to run every x (adjust the delay and interval as needed)
        long delay = 0;  // Delay before the first execution (in milliseconds)
        long interval = 30000;  // Interval between executions (every 30 sec for testing)
        timer.scheduleAtFixedRate(new SendRecurringRandomMessage(guild, outputChannel, randomQuoteSender), delay, interval);
    }

    private void writeAllMessagesToDB(Guild guild) {

        Optional<TextChannel> bogBotsChannel = guild.getTextChannels()
                .stream()
                .filter(channel -> channel.getName().equals("bogbot"))
                .findFirst();

        List<TextChannel> filteredTextChannels = guild.getTextChannels()
                .stream()
                .filter(channel -> !channel.getName().equals("bogbot"))
                .toList();

        for (TextChannel textChannel : filteredTextChannels) {
            randomQuoteSender.getDatabasePopulator().populateDB(textChannel);
        }

        System.out.println("Databases successfuly created");

        UnionTables joinTables = new UnionTables(logger, bogBotsChannel.get());
        joinTables.join(filteredTextChannels);
    }

    private void logHashMapSize(RandomQuoteSender randomQuoteSender) {
        logger.info("keysets in memory: {}", messageReader.getChannelMessageHistories().keySet());
    }
}
