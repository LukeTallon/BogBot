package org.bog.bot.botLogic;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.MyEventListener;

public class MyBot {
    public static void main(String[] args) {
        String token = "secret";
        try {
            JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .setMaxReconnectDelay(5000)
                    .addEventListeners(new MyEventListener()) // Create an event listener class
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
