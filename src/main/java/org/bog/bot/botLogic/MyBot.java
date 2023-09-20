package org.bog.bot.botLogic;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.MyEventListener;
import org.bog.bot.stuff.ConfigLoader;

import java.io.IOException;

public class MyBot {
    public static void main(String[] args) {
        try {
            // Load the token using ConfigLoader
            String token = ConfigLoader.loadToken();

            // Check if the token is not null or empty
            if (token != null && !token.isEmpty()) {
                JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .setMaxReconnectDelay(5000)
                        .addEventListeners(new MyEventListener()) // Create an event listener class
                        .build();
            } else {
                System.out.println("Token is missing or empty in config.yaml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
