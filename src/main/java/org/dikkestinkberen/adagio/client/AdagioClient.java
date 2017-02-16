package org.dikkestinkberen.adagio.client;

import org.dikkestinkberen.adagio.configuration.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Created by Bab on 16-2-2017.
 */
public class AdagioClient {
    private final static Logger logger = LoggerFactory.getLogger(AdagioClient.class);

    private static IDiscordClient client;

    public static void init() throws DiscordException {
        boolean done = false;

        client = new ClientBuilder().withToken(Config.TOKEN).build();
        client.getDispatcher().registerListener(new AdagioClient());

        while (!done) {
            try {
                logger.info("Logging in...");
                client.login();
                logger.info("Connected!");
                done = true;
            } catch (RateLimitException e) {
                logger.warn(String.format("Too many requests to the Discord service. Retrying in %d seconds...", e.getRetryDelay() / 1000));
                try {
                    Thread.sleep(e.getRetryDelay());
                } catch (InterruptedException e1) {
                    // I really don't care.
                }
            }
        }
    }

    public static boolean sendMessage(IChannel channel, String message) throws MissingPermissionsException, DiscordException {
        while (true) {
            try {
                logger.debug(String.format("Sending message:\n%s", message));
                channel.sendMessage(message);
                logger.trace("Message sent.");
                return true;
            } catch (DiscordException | MissingPermissionsException e) {
                throw e;
            } catch (RateLimitException e) {
                logger.warn("Too many messages, slow down.");
                try {
                    Thread.sleep(e.getRetryDelay());
                } catch (InterruptedException e1) {
                    // I really don't care.
                }
            }
        }
    }
}
