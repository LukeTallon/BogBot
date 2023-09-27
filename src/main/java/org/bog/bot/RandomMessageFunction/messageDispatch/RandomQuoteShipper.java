package org.bog.bot.RandomMessageFunction.messageDispatch;

import lombok.Data;
import org.bog.bot.POJOs.DiscordQuote;
import org.bog.bot.RandomMessageFunction.db.DatabaseRetriever;
import org.bog.bot.RandomMessageFunction.messageRetrieval.MessageFormatter;
import org.slf4j.Logger;

@Data
public class RandomQuoteShipper {

    private Logger logger;
    private String dbTableName;
    private MessageFormatter messageFormatter = new MessageFormatter();

    public RandomQuoteShipper(Logger logger) {
        this.logger = logger;
    }

    public String getRandomQuote(String dbTableName) {

        DiscordQuote randomFromDb = new DatabaseRetriever(logger).getRandomMessageFromDB(dbTableName);

        if (randomFromDb != null) {
            logger.info("returning a random from database!");
            return messageFormatter.formatMessageDetails(randomFromDb);
        } else {
            return "No messages in memory, please use '!dbload' to populate BogBot's database";
        }
    }

}