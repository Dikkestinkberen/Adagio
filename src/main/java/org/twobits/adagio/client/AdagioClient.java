package org.twobits.adagio.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twobits.adagio.audio.AdagioAudioManager;
import org.twobits.adagio.configuration.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The base class that handles logging in, threading and the like.
 */
public class AdagioClient {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(AdagioClient.class);
    private final static Map<String, AdagioAudioManager> audioManagers = new LinkedHashMap<>();
    private static IDiscordClient client;
    private IChannel audioChannel;

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

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        for (IGuild guild : client.getGuilds()) {
            audioManagers.put(guild.getID(), new AdagioAudioManager(guild));
        }
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent event) throws MissingPermissionsException, DiscordException {
        IMessage message = event.getMessage();
        IUser user = message.getAuthor();
        if (user.isBot()) {
            return;
        }

        String content = message.getContent();
        if (!content.startsWith(Config.PREFIX)) {
            return;
        }

        content = content.substring(Config.PREFIX.length());
        String command = content.substring(0, content.indexOf(' '));
        switch (command) {
            case "play":
                audioManagers.get(message.getGuild().getID()).play(message);
                audioChannel = message.getChannel();
        }
    }

    @EventSubscriber
    public void onTrackStart(TrackStartEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        sendMessage(audioChannel, "Now Playing!");
    }

    @EventSubscriber
    public void onTrackFinish(TrackFinishEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        sendMessage(audioChannel, "Finished!");
    }

    @SuppressWarnings({"UnusedReturnValue", "unused"})
    public static boolean sendMessage(IChannel channel, String message) throws MissingPermissionsException, DiscordException {
        try {
            logger.debug(String.format("Sending message:\t%s", message));
            channel.sendMessage(message);
            logger.trace("Message sent.");
            return true;
        } catch (RateLimitException e) {
            logger.warn("Too many messages, slow down.");
            return false;
        }
    }
}
