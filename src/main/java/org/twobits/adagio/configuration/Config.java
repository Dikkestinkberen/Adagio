package org.twobits.adagio.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The class that reads and supplies config information.
 */
public final class Config {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(Config.class);
    public static String TOKEN;

    public static void getPropertyValues() throws IOException {
        Properties prop = new Properties();

        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(Constants.CONFIG_FILE_NAME)) {

            if (inputStream == null) {
                throw new FileNotFoundException(String.format("Property file missing: %s", Constants.CONFIG_FILE_NAME));
            }

            prop.load(inputStream);

            TOKEN = prop.getProperty("token");
        }
    }
}
