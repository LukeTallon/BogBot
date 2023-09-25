package org.bog.bot.stages;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.messageRetrieval.MessageReader;
import org.bog.bot.db.DatabasePopulator;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;


import static org.bog.bot.Utils.Util.BOGBOT_CHANNEL_NAME;
import static org.bog.bot.Utils.Util.FRIENDS_SPOILER_CHANNEL;

@Data
public class MessageRetrievalStage {

    private Logger logger;
    private DatabasePopulator databasePopulator;
    public MessageRetrievalStage(Logger logger, DatabasePopulator databasePopulator) {
        this.logger = logger;
    }


    public CompletableFuture<Void> readMessagesInGuildAsync(Guild guild, TextChannel outputChannel, MessageReader messageReader) {
        return CompletableFuture.runAsync(() -> readMessagesInGuild(guild, outputChannel, messageReader));
    }

    private void readMessagesInGuild(Guild guild, TextChannel outputChannel, MessageReader messageReader) {

        List<TextChannel> filteredTextChannels = guild.getTextChannels()
                .stream()
                .filter(channel -> !channel.getName().equals(BOGBOT_CHANNEL_NAME) && !channel.getName().equals(FRIENDS_SPOILER_CHANNEL))
                .toList();

        for (TextChannel textChannel : filteredTextChannels) {
            logger.info("Reading messages from channel: {}", textChannel.getName());
            messageReader.populateMessages(textChannel, outputChannel);
        }
    }
}
