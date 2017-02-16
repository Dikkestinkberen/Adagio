package configuration;

/**
 * Created by Bab on 16-2-2017.
 */
public final class Constants {
    public final static String CONFIG_FILE_NAME = "config.properties";

    // Append a number to start autonumbering from, a space, then the url.
    public final static String YOUTUBE_DL_GET_FILENAMES_COMMAND = "-o \"%(autonumber)s.mp3\" --restrict-filenames --get-filename --autonumber-start ";
    public final static String YOUTUBE_DL_DOWNLOAD_COMMAND = "-x -o \"%(autonumber)s.%(ext)s\" --restrict-filenames --audio-format \"mp3\" --autonumber-start ";
}
