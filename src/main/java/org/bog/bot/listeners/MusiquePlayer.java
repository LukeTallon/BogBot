package org.bog.bot.listeners;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Widget;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MusiquePlayer extends ListenerAdapter {
    private final AudioPlayer player;
    private final DefaultAudioPlayerManager playerManager;
    private final Logger logger;
    private final TrackScheduler scheduler;

    public MusiquePlayer(DefaultAudioPlayerManager playerManager, AudioPlayer player, Logger logger, TrackScheduler scheduler) {
        this.playerManager = playerManager;
        this.player = player;
        this.logger = logger;
        this.scheduler = scheduler;

    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        String message = event.getMessage().getContentRaw();

        if(!message.startsWith("!yt")){
            return;
        }

        String[] parts = message.split(" ", 2);
        if (parts.length < 2) {
            logger.info("issue in the !youtube if statement within bogboteventlsitener");
            return;
        }

        Member member = event.getMember();
        if (member == null) {
            logger.info("not sure how this even happens");
            return;
        }

        GuildVoiceState voiceState = member.getVoiceState();
        if (voiceState == null || voiceState.getChannel() == null) {
            logger.info("user not in voice channel");
            event.getChannel().sendMessage("You need to be in a voice channel for this command to work!").queue();
            return;
        }
        VoiceChannel voiceChannel = member.getVoiceState().getChannel().asVoiceChannel();
        if (voiceChannel == null) {
            logger.info("User is not in a voice channel, send an error message");
            return;
        }

        TextChannel eventChannel = event.getChannel().asTextChannel(); // No need to cast
        if(eventChannel == null) {
            logger.info("TextChannel is not available");
            return;
        }

        playYoutube(parts[1], voiceChannel, event.getGuild().getAudioManager(), eventChannel);
    }




    private void playYoutube(String searchTerm, VoiceChannel voiceChannel, AudioManager audioManager, TextChannel textChannel) {


        // Load and play the audio from the provided YouTube link
        playerManager.loadItem("ytsearch: " + searchTerm, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                textChannel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                scheduler.queue(track);
                // Run openAudioConnection in a separate thread
                CompletableFuture.runAsync(() -> {
                    if(!audioManager.isConnected()) // Check if it's not connected before connecting
                        audioManager.openAudioConnection(voiceChannel);
                }).exceptionally(e -> {
                    // Handle exceptions, log them and/or notify users as appropriate
                    logger.error("Error while opening audio connection", e);
                    textChannel.sendMessage("Error occurred while connecting to the voice channel!").queue();
                    return null;
                });
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                // If the playlist is a search result, play the first track
                if(playlist.isSearchResult()) {
                    AudioTrack firstTrack = playlist.getTracks().get(0);
                    scheduler.queue(firstTrack.makeClone());
                    connectAudio(audioManager, voiceChannel, textChannel);
                } else { // If the playlist is from a direct link, enqueue all tracks
                    for (AudioTrack track : playlist.getTracks()) {
                        scheduler.queue(track.makeClone());
                    }
                    connectAudio(audioManager, voiceChannel, textChannel);
                }
            }

            @Override
            public void noMatches() {
                textChannel.sendMessage("No matches found for the provided link or search term!").queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                textChannel.sendMessageFormat("Could not play the requested track: %s", exception.getMessage()).queue();
                logger.error("Could not play the requested track: " + exception.getMessage());
            }
        });
    }

    private void connectAudio(AudioManager audioManager, VoiceChannel voiceChannel, TextChannel textChannel) {
        CompletableFuture.runAsync(() -> {
            if (!audioManager.isConnected()) // Check if it's not connected before connecting
                audioManager.openAudioConnection(voiceChannel);
        }).exceptionally(e -> {
            // Handle exceptions, log them, and/or notify users as appropriate
            logger.error("Error while opening audio connection", e);
            textChannel.sendMessage("Error occurred while connecting to the voice channel!").queue();
            return null;
        });
    }
}
