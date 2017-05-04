package org.twobits.adagio.configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A class that contains constants for the entire application.
 */
public final class Constants {
    public final static String CONFIG_FILE_NAME = "config.properties";
    public final static String YOUTUBE_DL_DOWNLOAD_FOLDER = "audio";
    public final static List<String> YOUTUBE_DL_GET_INFO_ARRAY = new ArrayList<>(Arrays.asList("-i", "-j", "--geo-bypass"));
    public final static List<String> YOUTUBE_DL_SEARCH_ARRAY = new ArrayList<>(Arrays.asList("-i", "-j", "--geo-bypass"));
    public final static List<String> YOUTUBE_DL_DOWNLOAD_ARRAY_FIRST = new ArrayList<>(Arrays.asList("-x", "--audio-format", "mp3", "--audio-quality", "4", "-i", "-o"));
    public final static List<String> YOUTUBE_DL_DOWNLOAD_ARRAY_SECOND = new ArrayList<>(Arrays.asList("--geo-bypass", "--no-progress"));
}
