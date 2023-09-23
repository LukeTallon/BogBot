package org.bog.bot.MessageDispatch;

import lombok.Data;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.MessageRetrieval.MessageFormatter;
import org.bog.bot.POJOs.DiscordQuote;
import org.bog.bot.db.DatabasePopulator;
import org.slf4j.Logger;

import static org.bog.bot.Utils.Utils.removeHyphensFromTableName;

@Data
public class RandomQuoteSender {

    private Logger logger;
    private DatabasePopulator databasePopulator;
    private MessageFormatter messageFormatter = new MessageFormatter();

    public RandomQuoteSender(Logger logger, DatabasePopulator databasePopulator) {
        this.logger = logger;
        this.databasePopulator = databasePopulator;
    }

    public String getRandomQuote(TextChannel channel) {

        String dbTableName = removeHyphensFromTableName(channel.getName().concat(channel.getId()));

        DiscordQuote randomFromDb = databasePopulator.getRandomMessageFromDB(dbTableName);

        if (randomFromDb != null) {
            logger.info("returning a random from database!");
            return messageFormatter.formatMessageDetails(randomFromDb);
        } else {
            return "No messages in memory, please use '!dbload' to populate BogBot's database";
        }
    }

}