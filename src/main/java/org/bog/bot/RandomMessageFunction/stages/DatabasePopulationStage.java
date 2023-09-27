package org.bog.bot.RandomMessageFunction.stages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.RandomMessageFunction.db.DatabasePopulator;
import org.bog.bot.RandomMessageFunction.db.UnionTables;
import org.bog.bot.RandomMessageFunction.messageDispatch.RandomQuoteShipper;
import org.bog.bot.RandomMessageFunction.messageRetrieval.MessageReader;
import org.slf4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static org.bog.bot.Utils.Util.BOGBOT_CHANNEL_NAME;
import static org.bog.bot.Utils.Util.FRIENDS_SPOILER_CHANNEL;

public class DatabasePopulationStage {

    private final Logger logger;
    private final RandomQuoteShipper randomQuoteShipper;
    private final MessageReader messageReader;
    private final DatabasePopulator databasePopulator;
    private final TextChannel outputChannel;

    public DatabasePopulationStage(Logger logger, RandomQuoteShipper randomQuoteShipper, MessageReader messageReader, DatabasePopulator databasePopulator, TextChannel outputChannel) {
        this.logger = logger;
        this.randomQuoteShipper = randomQuoteShipper;
        this.messageReader = messageReader;
        this.databasePopulator = databasePopulator;
        this.outputChannel = outputChannel;
    }

    public CompletableFuture<Void> writeAllMessagesToDB(Guild guild) {
        CompletableFuture<Void> allPopulated = CompletableFuture.allOf(
                messageReader.getPopulateFutures().toArray(new CompletableFuture[0])
        );

        logger.info("Populating database...");

        return allPopulated.thenCompose(v -> {
            List<TextChannel> filteredTextChannels = guild.getTextChannels()
                    .stream()
                    .filter(channel -> !channel.getName().equals(BOGBOT_CHANNEL_NAME) && !channel.getName().equals(FRIENDS_SPOILER_CHANNEL)).toList();

            for (TextChannel textChannel : filteredTextChannels) {
                databasePopulator.populateDB(textChannel);
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
