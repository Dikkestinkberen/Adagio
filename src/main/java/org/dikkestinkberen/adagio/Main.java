package org.dikkestinkberen.adagio;

import org.dikkestinkberen.adagio.client.AdagioClient;
import org.dikkestinkberen.adagio.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Bab on 16-2-2017.
 */
public class Main {
    private final static Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            logger.info("Initializing...");
            Config.getPropertyValues();
            AdagioClient.init();
        } catch (Exception e) {
            logger.error("A fatal error has occurred: ", e);
        }
        logger.info("Shutting down.");
    }
}
