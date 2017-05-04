package org.twobits.adagio.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twobits.adagio.audio.AdagioAudioManager;
import org.twobits.adagio.configuration.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.audio.AudioPlayer;
import sx.blah.discord.util.audio.events.TrackFinishEvent;
import sx.blah.discord.util.audio.events.TrackStartEvent;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The base class that handles logging in, threading and the like.
 */
public class AdagioClient {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(AdagioClient.class);
    private final static Map<Long, AdagioAudioManager> audioManagers = new LinkedHashMap<>();
    private final static Map<AudioPlayer.Track, IChannel> audioRequestTextChannels = new ConcurrentHashMap<>();
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

    public static void addTrackRequestChannel(AudioPlayer.Track track, IChannel requestChannel) {
        audioRequestTextChannels.put(track, requestChannel);
    }

    public static void removeTrackRequestChannel(AudioPlayer.Track track) {
        audioRequestTextChannels.remove(track);
    }

    @EventSubscriber
    public void onReady(ReadyEvent event) {
        for (IGuild guild : client.getGuilds()) {
            audioManagers.put(guild.getLongID(), new AdagioAudioManager(guild));
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
                IVoiceChannel requesterVoiceChannel = user.getVoiceStateForGuild(message.getGuild()).getChannel();
                AdagioAudioManager currentManager = audioManagers.get(message.getGuild().getLongID());
                if (requesterVoiceChannel == null) {
                    sendMessage(message.getChannel(), "You are not connected to a voice channel.");
                } else if (currentManager.isPlaying() && currentManager.getChannel() != requesterVoiceChannel) {
                    sendMessage(message.getChannel(), "The bot is already playing in a different channel.");
                } else {
                    currentManager.play(message);
                }
        }
    }

    @EventSubscriber
    public void onTrackStart(TrackStartEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        sendMessage(audioRequestTextChannels.get(event.getTrack()), "Now Playing!");
    }

    @EventSubscriber
    public void onTrackFinish(TrackFinishEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        sendMessage(audioRequestTextChannels.get(event.getOldTrack()), "Finished!");
        removeTrackRequestChannel(event.getOldTrack());
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
