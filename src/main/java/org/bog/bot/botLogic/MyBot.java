package org.bog.bot.botLogic;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.MyEventListener;
import org.bog.bot.MessageDispatch.RandomQuoteSender;
import org.bog.bot.MessageRetrieval.MessageReader;
import org.bog.bot.Utils.TokenLoader;
import org.bog.bot.db.DatabasePopulator;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
                        .build();
                jda.addEventListener(new MyEventListener(jda));

                // Create a single instance of DatabasePopulator
                DatabasePopulator databasePopulator = new DatabasePopulator(LoggerFactory.getLogger(DatabasePopulator.class));

                // Pass the same DatabasePopulator instance to RandomQuoteSender and MessageReader
                RandomQuoteSender randomQuoteSender = new RandomQuoteSender(LoggerFactory.getLogger(RandomQuoteSender.class), databasePopulator);
                MessageReader messageReader = new MessageReader(LoggerFactory.getLogger(MessageReader.class), databasePopulator);


            } else {
                System.out.println("Token is missing or empty in token.yaml");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
