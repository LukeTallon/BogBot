package org.bog.bot.Listeners;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.MessageDispatch.RandomQuoteShipper;
import org.bog.bot.MessageRetrieval.MessageReader;
import org.bog.bot.Stages.BeginSendingMessageStage;
import org.bog.bot.Stages.DatabasePopulationStage;
import org.bog.bot.Stages.MessageRetrievalStage;
import org.bog.bot.db.DatabasePopulator;
import org.slf4j.Logger;
import java.util.concurrent.CompletableFuture;

@Data
public class BotInitializer {

    private static Logger logger;
    private RandomQuoteShipper randomQuoteShipper;
    private MessageReader messageReader;
    private DatabasePopulator databasePopulator;

        public BotInitializer(Logger logger, RandomQuoteShipper randomQuoteShipper, MessageReader messageReader, DatabasePopulator databasePopulator) {
        this.logger = logger;
        this.randomQuoteShipper = randomQuoteShipper;
        this.messageReader = messageReader;
        this.databasePopulator = databasePopulator;
    }


    public void initializeBogBot(Guild guild, TextChannel outputChannel, String message) {
        if (message.equalsIgnoreCase("!setup")) {
            CompletableFuture<Void> setupFuture = new MessageRetrievalStage(logger,databasePopulator)
                    .readMessagesInGuildAsync(guild, outputChannel, messageReader)
                    .thenCompose(v -> new DatabasePopulationStage(logger,randomQuoteShipper,messageReader,databasePopulator,outputChannel).writeAllMessagesToDB(guild))
                    .thenCompose(v -> new BeginSendingMessageStage(logger, randomQuoteShipper).startSendingRecurringRandomMessageAsync(guild, outputChannel));

            setupFuture.exceptionally(e -> {
                logger.error("An error occurred during setup: ", e);
                return null;
            });
        }

        if (message.equalsIgnoreCase("!rq")) {
            String finalTableName = "combinedtable" + guild.getName().replaceAll("\\s", "");
            randomQuoteShipper.getRandomQuote(finalTableName);
        }

        if (message.equalsIgnoreCase("!restart")) {
            new BeginSendingMessageStage(logger, randomQuoteShipper).startSendingRecurringRandomMessage(guild, outputChannel);
        }
    }
}
