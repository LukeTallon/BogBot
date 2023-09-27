package org.bog.bot.listeners;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bog.bot.RandomMessageFunction.botManager.BotInitializer;
import org.bog.bot.RandomMessageFunction.botManager.HomeFinder;
import org.bog.bot.RandomMessageFunction.db.DatabasePopulator;
import org.bog.bot.RandomMessageFunction.messageDispatch.RandomQuoteShipper;
import org.bog.bot.RandomMessageFunction.messageRetrieval.MessageReader;
import org.slf4j.Logger;

import java.util.List;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = false)
public class RandomQuoteListener extends ListenerAdapter {


    public static final String SETUP_COMMAND = "!setup";
    private static Logger logger;
    private final String MESSAGE_TOO_LONG = "A random message was selected... However, it was over 2,000 characters, and therefore too long to send it. :(";
    private TextChannel outputChannelField;
    private RandomQuoteShipper randomQuoteShipper;
    private MessageReader messageReader;
    private DatabasePopulator databasePopulator;
    private JDA jda;
    private List<Guild> guilds;
    private HomeFinder botshome = new HomeFinder();

    public RandomQuoteListener(Logger logger, JDA jda, List<Guild> guilds) {
        this.jda = jda;
        this.guilds = guilds;
        RandomQuoteListener.logger = logger;
        this.databasePopulator = new DatabasePopulator(logger);
        this.randomQuoteShipper = new RandomQuoteShipper(logger);
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
                    Map<String, TextChannel> bogBotsHomes = botshome.bogBotsChannels(guilds);

                    if (bogBotsHomes.containsKey(guildId)) {
                        if (message.equalsIgnoreCase("!setup") || message.equalsIgnoreCase("!restart") || message.equalsIgnoreCase("!rq")) {
                            TextChannel outputChannel = bogBotsHomes.get(guildId);
                            BotInitializer botInitializer = new BotInitializer(logger, randomQuoteShipper, messageReader,databasePopulator);
                            botInitializer.initializeBogBot(guild, outputChannel, message);
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("An error occurred while processing a message:", e);
        }
    }
}
