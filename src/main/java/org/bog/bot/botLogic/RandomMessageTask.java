package org.bog.bot.botLogic;

import lombok.Data;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.bog.bot.stuff.RandomHistory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.stream.Collectors;

@Data
public class RandomMessageTask extends TimerTask {
    private static final Logger logger = LoggerFactory.getLogger(RandomMessageTask.class);


    private Guild guild;
    private TextChannel outputChannel;
    List<TextChannel> booneChannels = guild.getTextChannels();
    RandomHistory randomHistory = new RandomHistory(logger);



    public RandomMessageTask(Guild guild, TextChannel outputChannel) {
        this.guild = guild;
        this.outputChannel = outputChannel;
    }
    private int sizeRandomChannelSelector(List<TextChannel> filteredChannels){
        int size = filteredChannels.size();
        Random random = new Random();

        return random.nextInt(size);
    }

    private List<TextChannel> acceptableTextChannels(List<TextChannel> booneChannels){

        return booneChannels
                .stream()
                .filter(channel -> channel.getIdLong() != outputChannel.getIdLong())
                .collect(Collectors.toList());
    }
    @Override
    public void run() {

        List<TextChannel> acceptableChannels = acceptableTextChannels(booneChannels);

        String randomMessage = randomHistory.getRandomQuote(acceptableChannels.get(sizeRandomChannelSelector(acceptableChannels)));

        // Send the random message to the target channel
        outputChannel.sendMessage(randomMessage).queue();
    }
}
