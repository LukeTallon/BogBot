package org.bog.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.bog.bot.ytaudiofunctionality.GuildMusicManager;
import org.bog.bot.ytaudiofunctionality.YTInitializer;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class YTMusicListener extends ListenerAdapter {

    private static final String PLAY_COMMAND = "!play";
    private static final String SKIP_COMMAND = "!skip";
    private static final String DISCONNECT_COMMAND = "!dcbot";

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    private final Logger logger;

    public YTMusicListener(Logger logger) {
        this.playerManager = createPlayerManager();
        this.musicManagers = new HashMap<>();
        this.logger = logger;
    }

    private AudioPlayerManager createPlayerManager() {
        AudioPlayerManager manager = new DefaultAudioPlayerManager();
        manager.registerSourceManager(new YoutubeAudioSourceManager(true, "email@example.com", "PASSWORD!"));
        AudioSourceManagers.registerLocalSource(manager);
        return manager;
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        String messageContent = event.getMessage().getContentRaw();
        logger.info("Received a message: " + messageContent);

        String[] command = messageContent.split(" ", 2);
        processCommand(command, event);
    }

    private void processCommand(String[] command, MessageReceivedEvent event) {
        if (command.length < 1) return;

        YTInitializer initialize = new YTInitializer(command, event, playerManager, musicManagers, logger);

        switch (command[0]) {
            case PLAY_COMMAND:
                if (command.length > 1)
                    initialize.loadAndPlay(event.getChannel().asTextChannel(), command[1], event.getAuthor());
                break;
            case SKIP_COMMAND:
                initialize.skipTrack(event.getGuild());
                break;
            case DISCONNECT_COMMAND:
                initialize.disconnectBot(event.getGuild());
                break;
        }
    }
}
