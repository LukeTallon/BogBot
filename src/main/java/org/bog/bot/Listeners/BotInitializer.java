package org.bog.bot.Listeners;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.MessageDispatch.RandomQuoteSender;
import org.bog.bot.MessageDispatch.SendRecurringRandomMessage;
import org.bog.bot.MessageRetrieval.MessageReader;
import org.bog.bot.db.UnionTables;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import static org.bog.bot.Utils.Utils.loadTimerConfig;

@Data
public class BotInitializer {

    public static final String BOGBOT_CHANNEL_NAME = "bogbot";
    public static final String FRIENDS_SPOILER_CHANNEL = "spoilertalk";

    private static Logger logger;
    private RandomQuoteSender randomQuoteSender;
    private MessageReader messageReader;

    public BotInitializer(Logger logger, RandomQuoteSender randomQuoteSender, MessageReader messageReader) {
        this.logger = logger;
        this.randomQuoteSender = randomQuoteSender;
        this.messageReader = messageReader;
    }


    public void initializeBogBot(Guild guild, TextChannel outputChannel, String message) {
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
