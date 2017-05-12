package org.twobits.adagio.client;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twobits.adagio.audio.GuildMusicManager;
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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The base class that handles logging in, threading and the like.
 */
public class AdagioClient {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(AdagioClient.class);
    public static IDiscordClient client;
    private final static Map<Long, GuildMusicManager> musicManagers = new ConcurrentHashMap<>();
    private final static Map<Long, IVoiceChannel> currentVoiceChannels = new ConcurrentHashMap<>();
    private static AudioPlayerManager playerManager;

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
        playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
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

        message.delete();

        content = content.substring(Config.PREFIX.length());
        String command;
        try {
            command = content.substring(0, content.indexOf(' '));
        } catch (StringIndexOutOfBoundsException e) {
            command = content;
        }

        IVoiceChannel requesterVoiceChannel, currentVoiceChannel;
        GuildMusicManager musicManager;
        switch (command) {
            case "play":
                logger.debug("Play command issued: " + content);
                requesterVoiceChannel = user.getVoiceStateForGuild(message.getGuild()).getChannel();
                currentVoiceChannel = getCurrentVoiceChannel(message.getGuild());
                musicManager = getGuildAudioPlayer(message.getGuild());
                if (requesterVoiceChannel == null) {
                    sendMessage(message.getChannel(), "You are not connected to a voice channel.");
                } else if (musicManager.hasSong() && requesterVoiceChannel != currentVoiceChannel) {
                    sendMessage(message.getChannel(), "The bot is already playing in a different channel.");
                } else {
                    logger.debug("Accepted play command.");
                    setCurrentVoiceChannel(message.getGuild(), requesterVoiceChannel);
                    loadAndPlay(message, content.substring(5));
                }
                return;
            case "skip":
                logger.debug("Skip command issued.");
                requesterVoiceChannel = user.getVoiceStateForGuild(message.getGuild()).getChannel();
                currentVoiceChannel = getCurrentVoiceChannel(message.getGuild());
                musicManager = getGuildAudioPlayer(message.getGuild());
                if (!musicManager.hasSong()) {
                    sendMessage(message.getChannel(), "The bot is not currently playing any music.");
                } else if (requesterVoiceChannel == null || requesterVoiceChannel != currentVoiceChannel) {
                    sendMessage(message.getChannel(), "You are not in the same channel as the bot.");
                } else {
                    logger.debug("Accepted skip command.");
                    sendMessage(message.getChannel(), "Song skipped.");
                    musicManager.scheduler.skip();
                }
        }
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

    private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
        long guildId = guild.getLongID();
        GuildMusicManager musicManager = musicManagers.computeIfAbsent(guildId,
                k -> new GuildMusicManager(playerManager, guild));

        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    private static IVoiceChannel getCurrentVoiceChannel(IGuild guild) {
        return currentVoiceChannels.get(guild.getLongID());
    }

    private static void setCurrentVoiceChannel(IGuild guild, IVoiceChannel channel) {
        if (channel != null) {
            currentVoiceChannels.put(guild.getLongID(), channel);
        } else {
            currentVoiceChannels.remove(guild.getLongID());
        }
    }

    private void loadAndPlay(IMessage request, String trackUrl) {
        GuildMusicManager musicManager = getGuildAudioPlayer(request.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack audioTrack) {
                //Song added to queue, can now be played.
                sendMessage(request.getChannel(), "Song added to queue: " + audioTrack.getInfo().title);

                play(request, musicManager, audioTrack);
            }

            @Override
            public void playlistLoaded(AudioPlaylist audioPlaylist) {

            }

            @Override
            public void noMatches() {
                logger.info("No matches.");
            }

            @Override
            public void loadFailed(FriendlyException e) {
                logger.info("Load failed: ", e);
            }
        });
    }

    private void play(IMessage request, GuildMusicManager musicManager, AudioTrack track) {
        getCurrentVoiceChannel(request.getGuild()).join();
        musicManager.scheduler.queue(request, track);
    }
}
