package org.bog.bot.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

public class DatabaseLoginLoader {

private static final Logger logger = LoggerFactory.getLogger(DatabaseLoginLoader.class);

    public static String[] loadDBloginInfo()  {
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

            return new String[]{dbUrl,username,password};
        } catch (IOException e) {
            logger.error("Error in DatabaseLoginLoader", e);
            throw new RuntimeException(e);
        }
    }
}
