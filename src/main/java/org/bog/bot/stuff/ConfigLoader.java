package org.bog.bot.stuff;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class ConfigLoader {
    public static String loadToken() throws IOException {
        // Load the config.yaml file
        Path configPath = Paths.get("src/main/resources/config.yaml");
        try (InputStream inputStream = Files.newInputStream(configPath)) {
            // Parse the YAML content
            Yaml yaml = new Yaml();
            Map<String, String> yamlData = yaml.load(inputStream);

            // Get the token from the YAML data
            String token = yamlData.get("token");

            return token;
        }
    }
}
