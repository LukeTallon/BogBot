package org.bog.bot.ytaudiofunctionality;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;

import java.util.Map;

public class YTInitializer {

    private String[] command;
    private MessageReceivedEvent event;
    private AudioPlayerManager playerManager;
    private Map<Long, GuildMusicManager> musicManagers;
    private final Logger logger;
    private static final String CHANNEL_NAME = "bogbot";

    public YTInitializer(String[] command, MessageReceivedEvent event, AudioPlayerManager playerManager, Map<Long, GuildMusicManager> musicManagers, Logger logger) {
        this.command = command;
        this.event = event;
        this.logger = logger;
        this.playerManager = playerManager;
        this.musicManagers = musicManagers;
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl, final User user) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, "ytsearch: " + trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                sendMessage(channel.getGuild(), "Adding to queue " + track.getInfo().title);
                logger.info("Track loaded: " + track.getInfo().title); // Log when track is loaded
                play(channel.getGuild(), musicManager, track, user);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                sendMessage(channel.getGuild(), "Adding to queue " + firstTrack.getInfo().title);

                play(channel.getGuild(), musicManager, firstTrack, user);
            }

            @Override
            public void noMatches() {
                sendMessage(channel.getGuild(), "Nothing found by " + trackUrl);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Error loading track: " + exception.getMessage(), exception); // Log error with stack trace
                sendMessage(channel.getGuild(), "Could not play: " + exception.getMessage());
            }
        });
    }

    public void skipTrack(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.scheduler.nextTrack();
        sendMessage(guild, "Skipped to next track.");
    }

    public void disconnectBot(Guild guild) {
        GuildMusicManager musicManager = getGuildAudioPlayer(guild);
        musicManager.player.stopTrack(); // Stop the currently playing track

        AudioManager audioManager = guild.getAudioManager();
        if (audioManager.isConnected() || audioManager.getConnectionStatus().shouldReconnect()) {
            audioManager.closeAudioConnection(); // Disconnect the bot from the voice channel
        }
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, User user) {
        connectToVoiceChannelOfCommandIssuer(guild.getAudioManager(), user);
        logger.info("Playing track: " + track.getInfo().title + " in guild: " + guild.getName()); // Log when track starts playing
        musicManager.scheduler.queue(track);
    }


    private void connectToVoiceChannelOfCommandIssuer(AudioManager audioManager, User user) {
        Guild guild = audioManager.getGuild();
        Member member = guild.getMember(user); // get the member representation of the user in this guild
        if (member != null && member.getVoiceState() != null && member.getVoiceState().getChannel() != null) {
            // The member is in a voice channel in this guild
            audioManager.openAudioConnection(member.getVoiceState().getChannel());
        } else {
            // Handle the case where the user is not in a voice channel
            // You might want to send a message to the text channel informing the user that they need to be in a voice channel
        }
    }


    private TextChannel getBogbotChannel(Guild guild) {
        return guild.getTextChannelsByName(CHANNEL_NAME, true).stream().findFirst().orElse(null);
    }

    private void sendMessage(Guild guild, String message) {
        TextChannel channel = getBogbotChannel(guild);
        if (channel != null) channel.sendMessage(message).queue();
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            playerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);
            playerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());



        return musicManager;
    }


}
