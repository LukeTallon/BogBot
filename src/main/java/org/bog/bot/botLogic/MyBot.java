package org.bog.bot.botLogic;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.MyEventListener;
import org.bog.bot.stuff.TokenLoader;

import java.io.IOException;
import java.util.Timer;

public class MyBot {
    public static void main(String[] args) {
        try {
            // Load the token using TokenLoader
            String token = TokenLoader.loadToken();

            // Check if the token is not null or empty
            if (token != null && !token.isEmpty()) {
                JDA jda = JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .setMaxReconnectDelay(5000)
                        .addEventListeners(new MyEventListener()) // Create an event listener class
                        .build();

                Timer timer = new Timer();

                // Schedule a task to run every hour (adjust the delay and interval as needed)
                long delay = 0;  // Delay before the first execution (in milliseconds)
                long interval = 3600000;  // Interval between executions (e.g., every hour)

                Guild guild = jda.getGuildById(431710770737184771L);
                TextChannel outputChannel = guild.getTextChannelById(1154950416761630751L);
                timer.scheduleAtFixedRate(new RandomMessageTask(guild, outputChannel), delay, interval);
            } else {
                System.out.println("Token is missing or empty in token.yaml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
