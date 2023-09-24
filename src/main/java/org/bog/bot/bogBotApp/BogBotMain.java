package org.bog.bot.bogBotApp;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.BogBotEventListener;

import java.io.IOException;
import java.util.List;

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

                List<Guild> guilds = jda.awaitReady().getGuilds();

                jda.addEventListener(new BogBotEventListener(jda, guilds));


            } else {
                System.out.println("Token is missing or empty in token.yaml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
