package org.twobits.adagio.audio;

/**
 * Created by Bab on 16-3-2017.
 */
public enum DownloadResult {
    NOT_FINISHED (-1),
    SUCCESS (0),
    UNKNOWN_ERROR (1),
    // NO_YOUTUBEDL signifies that the command failed to even run, meaning youtube-dl is not present.
    NO_YOUTUBEDL (2),
    // NO_OUTPUT signifies that there was no output available for the download. This might require cleaning up.
    NO_OUTPUT (3);

    private final int value;
    DownloadResult(int value) {
        this.value = value;
    }
}
