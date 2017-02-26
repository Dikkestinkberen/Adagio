package org.twobits.adagio;

import org.twobits.adagio.client.AdagioClient;
import org.twobits.adagio.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entry point for the application.
 */
public class Main {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        // Note that initialization will return fairly quickly and
        // the program will keep running when this method completes.
        try {
            logger.info("Initializing...");
            Config.getPropertyValues();
            AdagioClient.init();
        } catch (Exception e) {
            logger.error("A fatal error has occurred: ", e);
        }
        logger.info("Initialization complete.");
    }
}
