package org.bog.bot.Utils;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class TokenLoader {
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
}
