package org.twobits.adagio.audio;

/**
 * Created by Bab on 16-3-2017.
 */
public interface DownloadListener {
    void notifyOfCompletion(DownloadResult result, AudioFile audioFile);
    String getFolder();
}
