package org.bog.bot.Stages;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.MessageDispatch.RandomQuoteShipper;
import org.bog.bot.MessageDispatch.SendRecurringRandomMessage;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.Timer;
import java.util.concurrent.CompletableFuture;

import static org.bog.bot.Utils.Util.loadTimerConfig;

public class BeginSendingMessageStage {
    private Logger logger;
    private RandomQuoteShipper randomQuoteShipper;

    public BeginSendingMessageStage(Logger logger, RandomQuoteShipper randomQuoteShipper) {
        this.logger = logger;
        this.randomQuoteShipper = randomQuoteShipper;
    }

    public CompletableFuture<Void> startSendingRecurringRandomMessageAsync(Guild guild, TextChannel outputChannel) {
        return CompletableFuture.runAsync(() -> startSendingRecurringRandomMessage(guild, outputChannel));
    }

    public void startSendingRecurringRandomMessage(Guild guild, TextChannel outputChannel) {
        Timer timer = new Timer();
        long[] timerValues;

        try {
            timerValues = loadTimerConfig();
        } catch (IOException e) {
            logger.error("Error loading timer config: ", e);
            throw new RuntimeException(e);
        }

        long delay = timerValues[0];  // Delay before the first execution
        logger.info("Delay upon first starting (in ms): " + delay);
        long interval = timerValues[1];  // Interval between executions
        logger.info("Interval between messages (in ms): " + interval);
        timer.scheduleAtFixedRate(new SendRecurringRandomMessage(guild, outputChannel, randomQuoteShipper), delay, interval);
    }
}
