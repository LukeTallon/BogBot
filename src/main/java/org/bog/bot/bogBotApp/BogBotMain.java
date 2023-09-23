package org.bog.bot.bogBotApp;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.MyEventListener;

import java.io.IOException;

import static org.bog.bot.Utils.Utils.loadToken;

public class BogBotMain {
    public static void main(String[] args) {
        try {
            // Load the token using TokenLoader
            String token = loadToken();

            // Check if the token is not null or empty
            if (token != null && !token.isEmpty()) {
                JDA jda = JDABuilder.createDefault(token)
                        .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                        .setMaxReconnectDelay(5000)
                        .build();
                jda.addEventListener(new MyEventListener(jda));

            } else {
                System.out.println("Token is missing or empty in token.yaml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
