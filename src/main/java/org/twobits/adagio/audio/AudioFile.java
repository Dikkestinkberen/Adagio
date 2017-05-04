package org.twobits.adagio.audio;

import sx.blah.discord.handle.obj.IChannel;

/**
 * Class that represents an audio file.
 */
public class AudioFile {
    private AudioFileAvailability audioFileAvailability;
    private final String id;
    private final String title;
    private final String url;
    private final IChannel requestChannel;

    public AudioFile() {
        this.audioFileAvailability = AudioFileAvailability.UNAVAILABLE;
        this.id = null;
        this.title = null;
        this.url = null;
        this.requestChannel = null;
    }

    public AudioFile(String id, String title, String url, IChannel requestChannel) {
        this.audioFileAvailability = AudioFileAvailability.DOWNLOADABLE;
        this.id = id;
        this.title = title;
        this.url = url;
        this.requestChannel = requestChannel;
    }

    public boolean isDownloadable() {
        return audioFileAvailability == AudioFileAvailability.DOWNLOADABLE;
    }

    public boolean isPlayable() {
        return audioFileAvailability == AudioFileAvailability.PLAYABLE;
    }

    public void setDownloaded() {
        audioFileAvailability = AudioFileAvailability.PLAYABLE;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getUrl() {
        return url;
    }

    public String getFileNameWithoutExtension() {
        return id;
    }

    public String getFileName() {
        return id + ".mp3";
    }

    public IChannel getRequestChannel() {
        return requestChannel;
    }
}
