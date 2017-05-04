package org.twobits.adagio.audio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twobits.adagio.configuration.Constants;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.audio.AudioPlayer;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Bab on 16-3-2017.
 */
public class AdagioAudioManager implements DownloadListener{
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(AdagioAudioManager.class);

    private final AudioPlayer player;
    private final String folder;
    private final ConcurrentLinkedQueue<AudioFile> downloadQueue = new ConcurrentLinkedQueue<>();
    private AtomicBoolean ready = new AtomicBoolean(true);
    private IGuild guild;

    public AdagioAudioManager(IGuild guild) {
        this.guild = guild;
        Path currentRelativePath = Paths.get( Constants.YOUTUBE_DL_DOWNLOAD_FOLDER + File.separator + guild.getID());
        this.folder = currentRelativePath.toAbsolutePath().toString() + File.separator;
        player = AudioPlayer.getAudioPlayerForGuild(guild);
    }

    @Override
    public void notifyOfCompletion(DownloadResult result, AudioFile audioFile) {
        if (result == DownloadResult.SUCCESS) {
            logger.debug("Download of \"" + audioFile.getTitle() + "\" was successful");
            try {
                player.queue(new File(folder + audioFile.getFileName()));
                player.setPaused(false);
            } catch (Exception e) {
                logger.warn("Failed to queue audio file with id " + audioFile.getId() + ": ", e);
            }
        } else {
            logger.warn("Download of \"" + audioFile.getTitle() + "\" failed: " + result.toString());
            // give some sort of error to the discord chat
        }

        if (downloadQueue.isEmpty()) {
            logger.debug("Queue empty for guild " + guild.getName());
            ready.set(true);
        } else {
            logger.trace("Downloading next item in queue...");
            new DownloadManager(downloadQueue.remove(), this).run();
        }
    }

    @Override
    public String getFolder() {
        return folder;
    }

    public void play(IMessage message) {
        List<IVoiceChannel> potentialChannels = message.getAuthor().getConnectedVoiceChannels();
        for (IVoiceChannel channel : potentialChannels) {
            if (channel.getGuild().equals(guild)) {
                try {
                    channel.join();
                } catch (MissingPermissionsException e) {
                    logger.debug("No permission to join " + channel.getName());
                }
            }
        }

        String content = message.getContent();
        List<AudioFile> audioFiles = DownloadManager.getAudioInfo(content.substring(content.indexOf(' ') + 1));
        for (AudioFile audioFile : audioFiles) {
            if (audioFile.isDownloadable()) {
                if (ready.compareAndSet(true, false)) {
                    new DownloadManager(audioFile, this).run();
                } else {
                    downloadQueue.add(audioFile);
                }
            }
        }
    }
}
