package org.bog.bot.bogBotApp;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.BogBotEventListener;
import org.bog.bot.Utils.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class BogBotMain {

    private static final Logger logger = LoggerFactory.getLogger(BogBotMain.class);


    public static void main(String[] args) {
        try {
            new BogBotMain().start();
        } catch (Exception e) {
            logger.error("Error starting the bot", e);
        }
    }

    public void start() throws IOException, InterruptedException {
        String token = Util.loadToken(); // Example assumes Util.loadToken handles exceptions gracefully and logs them
        if (token == null || token.isEmpty()) {
            logger.error("Bot token is missing or empty. Please check your configuration.");
            return;
        }

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                .setMaxReconnectDelay(5000)
                .build();


        List<Guild> guilds = jda.awaitReady().getGuilds();

        jda.addEventListener(new BogBotEventListener(logger, jda, guilds));
    }
}
