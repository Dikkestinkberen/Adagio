package org.twobits.adagio.configuration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static org.twobits.adagio.configuration.Constants.*;

/**
 * The class that reads and supplies config information.
 */
public final class Config {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(Config.class);
    public static String TOKEN;
    public static String YOUTUBE_DL_COMMAND;
    public static String PREFIX;
    public final static List<String> YOUTUBE_DL_GET_INFO_COMMAND = Collections.unmodifiableList(Constants.YOUTUBE_DL_GET_INFO_ARRAY);
    public final static List<String> YOUTUBE_DL_SEARCH_COMMAND = Collections.unmodifiableList(YOUTUBE_DL_SEARCH_ARRAY);
    public final static List<String> YOUTUBE_DL_DOWNLOAD_COMMAND_FIRST = Collections.unmodifiableList(YOUTUBE_DL_DOWNLOAD_ARRAY_FIRST);
    public final static List<String> YOUTUBE_DL_DOWNLOAD_COMMAND_SECOND = Collections.unmodifiableList(YOUTUBE_DL_DOWNLOAD_ARRAY_SECOND);

    public static void getPropertyValues() throws IOException {
        Properties prop = new Properties();

        try (InputStream inputStream = Config.class.getClassLoader().getResourceAsStream(Constants.CONFIG_FILE_NAME)) {

            if (inputStream == null) {
                throw new FileNotFoundException(String.format("Property file missing: %s", Constants.CONFIG_FILE_NAME));
            }

            prop.load(inputStream);

            TOKEN = prop.getProperty("token");
            YOUTUBE_DL_COMMAND = prop.getProperty("youtubedlCommand");
            PREFIX = prop.getProperty("prefix");

            YOUTUBE_DL_GET_INFO_ARRAY.add(0, YOUTUBE_DL_COMMAND);
            YOUTUBE_DL_SEARCH_ARRAY.add(0, YOUTUBE_DL_COMMAND);
            YOUTUBE_DL_DOWNLOAD_ARRAY_FIRST.add(0, YOUTUBE_DL_COMMAND);
        }
    }
}
