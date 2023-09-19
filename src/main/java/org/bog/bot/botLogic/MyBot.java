package org.bog.bot.botLogic;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.bog.bot.Listeners.MyEventListener;

public class MyBot {
    public static void main(String[] args) {
        String token = "MTE1MjYxMzc2MTM2Mjk2MDQ4NA.GF1W2R.NxIJEEUpLWGzvbtmxP4cD7aThM1eaC9F6lC3ms";
        try {
            JDABuilder.createDefault(token)
                    .enableIntents(GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.MESSAGE_CONTENT)
                    .addEventListeners(new MyEventListener()) // Create an event listener class
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
