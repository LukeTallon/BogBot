package org.bog.bot.Utils;

import net.dv8tion.jda.api.entities.Message;
import org.bog.bot.POJOs.DiscordQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Util {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    private static final Logger logger = LoggerFactory.getLogger(Util.class);

    public static final String BOGBOT_CHANNEL_NAME = "bogbot";
    public static final String FRIENDS_SPOILER_CHANNEL = "spoilertalk";
    private static final String TOKEN_PATH = "/token.yaml";
    private static final String TIMER_PATH = "/timerConfig.yaml";
    private static final String DB_CONFIG_PATH = "/dbConfig.yaml";

    //PostgreSQL seems to not like hyphens
    public static String removeHyphensFromTableName(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        int index = sb.indexOf("-");
        while (index != -1) {
            sb.replace(index, index + 1, ""); // Remove the hyphen
            index = sb.indexOf("-", index + 1); // Find the next hyphen
        }
        return sb.toString();
    }

    public static DiscordQuote discordQuoteBuilder(Message message) {
        // Initialize a string to store image URLs
        StringBuilder imageUrls = new StringBuilder();

        // Loop through the attachments to find image URLs
        message.getAttachments().stream()
                .filter(Message.Attachment::isImage)
                .forEach(attachment -> imageUrls.append(attachment.getUrl()).append(" \n"));

        String allImageUrls = imageUrls.toString();


        // Build and return the DiscordQuote object
        return DiscordQuote.builder()
                .id(message.getId())
                .contentRaw(message.getContentRaw())
                .author(message.getAuthor().getEffectiveName())
                .dateOfMessage(formatter.format(message.getTimeCreated()))
                .conditionalImage(allImageUrls)
                .jumpUrl(message.getJumpUrl())
                .build();
    }

    public static String loadToken() throws IOException {
        try (InputStream inputStream = Util.class.getResourceAsStream(TOKEN_PATH)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + TOKEN_PATH);
            }

            // Parse the YAML content
            Yaml yaml = new Yaml();
            Map<String, String> yamlData = yaml.load(inputStream);

            // Get the token from the YAML data
            return yamlData.get("token");
        } catch (IOException e) {
            logger.error("Error in loadToken", e);
            throw new RuntimeException(e);
        }
    }

    public static long[] loadTimerConfig() throws IOException {
        try (InputStream inputStream = Util.class.getResourceAsStream(TIMER_PATH)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + TIMER_PATH);
            }

            // Parse the YAML content
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);

            // Get the values from the YAML data and cast them to long
            long interval = ((Number) yamlData.get("interval")).longValue() * 60000L;
            long delay = ((Number) yamlData.get("delay")).longValue() * 1000L;

            return new long[]{delay, interval};
        } catch (IOException e) {
            logger.error("Error in loadTimerConfig", e);
            throw new RuntimeException(e);
        }
    }

    public static String[] loadDBloginInfo() {
        try (InputStream inputStream = Util.class.getResourceAsStream(DB_CONFIG_PATH)) {
            if (inputStream == null) {
                throw new IOException("Resource not found: " + DB_CONFIG_PATH);
            }

            // Parse the YAML content
            Yaml yaml = new Yaml();
            Map<String, String> yamlData = yaml.load(inputStream);

            // Get the token from the YAML data
            String dbUrl = yamlData.get("dbUrl");
            String username = yamlData.get("username");
            String password = yamlData.get("password");

            return new String[]{dbUrl, username, password};
        } catch (IOException e) {
            logger.error("Error in DatabaseLoginLoader", e);
            throw new RuntimeException(e);
        }
    }
}
