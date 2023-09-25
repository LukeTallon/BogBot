package org.bog.bot.Listeners;

import lombok.Data;
import lombok.EqualsAndHashCode;
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

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import static org.bog.bot.Utils.Utils.loadTimerConfig;

@Data
@EqualsAndHashCode(callSuper = false)
public class BogBotEventListener extends ListenerAdapter {

    public static final String BOGBOT_CHANNEL_NAME = "bogbot";
    public static final String FRIENDS_SPOILER_CHANNEL = "spoilertalk";
    public static final String SETUP_COMMAND = "!setup";
    private static Logger logger;
    private final String MESSAGE_TOO_LONG = "A random message was selected... However, it was over 2,000 characters, and therefore too long to send it. :(";
    private TextChannel outputChannelField;
    private RandomQuoteSender randomQuoteSender;
    private MessageReader messageReader;
    private DatabasePopulator databasePopulator;
    private JDA jda;
    private List<Guild> guilds;

    public BogBotEventListener(Logger logger, JDA jda, List<Guild> guilds) {
        this.jda = jda;
        this.guilds = guilds;
        this.logger = logger;
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
                    .filter(channel -> channel.getName().equals(BOGBOT_CHANNEL_NAME))
                    .findFirst();

            textChannelOptional.ifPresent(textChannel -> resultMap.put(guild.getId(), textChannel));
        }

        return resultMap;
    }


    private void initializeBogBot(Guild guild, TextChannel outputChannel, String message) {
        if (message.equalsIgnoreCase("!setup")) {
            CompletableFuture<Void> setupFuture = readMessagesInGuildAsync(guild, outputChannel)
                    .thenCompose(v -> CompletableFuture.runAsync(() -> outputChannel.sendMessage("Populating database...").queue()))
                    .thenCompose(v -> writeAllMessagesToDB(guild))
                    .thenCompose(v -> startSendingRecurringRandomMessageAsync(guild, outputChannel));

            setupFuture.exceptionally(e -> {
                logger.error("An error occurred during setup: ", e);
                return null;
            });
        }

        if (message.equalsIgnoreCase("!restart")) {
            startSendingRecurringRandomMessage(guild, outputChannel);
        }
    }


    private CompletableFuture<Void> readMessagesInGuildAsync(Guild guild, TextChannel outputChannel) {
        return CompletableFuture.runAsync(() -> readMessagesInGuild(guild, outputChannel));
    }

    private void readMessagesInGuild(Guild guild, TextChannel outputChannel) {

        List<TextChannel> filteredTextChannels = guild.getTextChannels()
                .stream()
                .filter(channel -> !channel.getName().equals(BOGBOT_CHANNEL_NAME) && !channel.getName().equals(FRIENDS_SPOILER_CHANNEL))
                .toList();

        for (TextChannel textChannel : filteredTextChannels) {
            System.out.println(textChannel.getName());
            messageReader.populateMessages(textChannel, outputChannel);
        }
    }

    private CompletableFuture<Void> startSendingRecurringRandomMessageAsync(Guild guild, TextChannel outputChannel) {
        return CompletableFuture.runAsync(() -> startSendingRecurringRandomMessage(guild, outputChannel));
    }


    private void startSendingRecurringRandomMessage(Guild guild, TextChannel outputChannel) {
        Timer timer = new Timer();
        long[] timerValues;

        try {
            timerValues = loadTimerConfig();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        long delay = timerValues[0];  // Delay before the first execution (in milliseconds)
        long interval = timerValues[1];  // Interval between executions (every 30 sec for testing)
        timer.scheduleAtFixedRate(new SendRecurringRandomMessage(guild, outputChannel, randomQuoteSender), delay, interval);
    }

    private CompletableFuture<Void> writeAllMessagesToDB(Guild guild) {
        CompletableFuture<Void> allPopulated = CompletableFuture.allOf(
                messageReader.getPopulateFutures().toArray(new CompletableFuture[0])
        );

        return allPopulated.thenCompose(v -> {
            List<TextChannel> filteredTextChannels = guild.getTextChannels()
                    .stream()
                    .filter(channel -> !channel.getName().equals(BOGBOT_CHANNEL_NAME) && !channel.getName().equals(FRIENDS_SPOILER_CHANNEL)).toList();

            for (TextChannel textChannel : filteredTextChannels) {
                randomQuoteSender.getDatabasePopulator().populateDB(textChannel);
            }

            logger.info("Databases successfully created");

            Optional<TextChannel> bogBotsChannel = guild.getTextChannels()
                    .stream()
                    .filter(channel -> channel.getName().equals(BOGBOT_CHANNEL_NAME))
                    .findFirst();

            if (bogBotsChannel.isPresent()) {
                UnionTables joinTables = new UnionTables(logger, bogBotsChannel.get());
                return CompletableFuture.runAsync(() -> joinTables.join(filteredTextChannels));
            } else {
                logger.error("bogBotsChannel is not present");
                // Returning a completed CompletableFuture exceptionally as bogBotsChannel is not present.
                return CompletableFuture.failedFuture(new IllegalStateException("bogBotsChannel is not present"));
            }
        }).exceptionally(e -> {
            logger.error("Error occurred while writing messages to DB", e);
            return null;
        });
    }
}
