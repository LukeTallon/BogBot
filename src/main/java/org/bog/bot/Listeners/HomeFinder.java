package org.bog.bot.Listeners;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class HomeFinder {

    public static final String BOGBOT_CHANNEL_NAME = "bogbot";

    public Map<String, TextChannel> bogBotsChannels(List<Guild> guilds) {
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
}
