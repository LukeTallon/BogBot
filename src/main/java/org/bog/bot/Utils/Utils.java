package org.bog.bot.Utils;

import net.dv8tion.jda.api.entities.Message;
import org.bog.bot.POJOs.DiscordQuote;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Utils {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    private static final Logger logger = LoggerFactory.getLogger(Utils.class);

    //PostgreSQL seems to not like hyphens
    public static String removeHyphensFromTableName(String tableName) {
        StringBuilder sb = new StringBuilder(tableName);
        int index = sb.indexOf("-");
        while (index != -1) {
            sb.replace(index, index + 1, ""); // Remove the hyphen
            index = sb.indexOf("-", index + 1); // Find the next hyphen
        }
        String modifiedTableName = sb.toString();
        return modifiedTableName;
    }

    public static DiscordQuote discordQuoteBuilder(Message message) {
        // Initialize a string to store image URLs
        String imageUrls = null;

        // Loop through the attachments to find image URLs
        for (Message.Attachment attachment : message.getAttachments()) {
            if (attachment.isImage()) {
                // If it's the first image URL, initialize the string
                if (imageUrls == null) {
                    imageUrls = attachment.getUrl() + " \n";
                } else {
                    // If it's not the first, append it with a space and newline
                    imageUrls += attachment.getUrl() + " \n";
                }
            }
        }

        // Build and return the DiscordQuote object
        return DiscordQuote.builder()
                .id(message.getId())
                .contentRaw(message.getContentRaw())
                .author(message.getAuthor().getEffectiveName())
                .dateOfMessage(formatter.format(message.getTimeCreated()))
                .conditionalImage(imageUrls)
                .jumpUrl(message.getJumpUrl())
                .build();
    }

    public static String loadToken() throws IOException {
        // Load the token.yaml file
        Path tokenPath = Paths.get("src/main/resources/token.yaml");
        try (InputStream inputStream = Files.newInputStream(tokenPath)) {
            // Parse the YAML content
            Yaml yaml = new Yaml();
            Map<String, String> yamlData = yaml.load(inputStream);

            // Get the token from the YAML data
            String token = yamlData.get("token");

            return token;
        }
    }

    public static long[] loadTimerConfig() throws IOException {
        // Load the token.yaml file
        Path tokenPath = Paths.get("src/main/resources/timerConfig.yaml");
        try (InputStream inputStream = Files.newInputStream(tokenPath)) {
            // Parse the YAML content
            Yaml yaml = new Yaml();
            Map<String, Object> yamlData = yaml.load(inputStream);

            // Get the values from the YAML data and cast them to long
            long interval = ((Number) yamlData.get("interval")).longValue() * 60000L;
            long delay = ((Number) yamlData.get("delay")).longValue() * 1000L;

            return new long[]{delay,interval};
        }
    }

    public static String[] loadDBloginInfo() {
        // Load the token.yaml file
        Path dbConfigPath = Paths.get("src/main/resources/dbConfig.yaml");
        try (InputStream inputStream = Files.newInputStream(dbConfigPath)) {
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
