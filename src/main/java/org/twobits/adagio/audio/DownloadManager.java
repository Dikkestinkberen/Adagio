package org.twobits.adagio.audio;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twobits.adagio.configuration.Config;
import org.twobits.adagio.configuration.Constants;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Bab on 4-3-2017.
 */
public class DownloadManager implements Runnable {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(DownloadManager.class);
    private final AudioFile audioFile;
    private final DownloadListener listener;

    public DownloadManager(AudioFile audioFile, DownloadListener listener) {
        this.audioFile = audioFile;
        this.listener = listener;
    }

    private DownloadResult getAudioFile() {
        List<String> command = new ArrayList<>(Config.YOUTUBE_DL_DOWNLOAD_COMMAND_FIRST);
        command.add(listener.getFolder() + audioFile.getFileNameWithoutExtension() + ".%(ext)s");
        command.addAll(Config.YOUTUBE_DL_DOWNLOAD_COMMAND_SECOND);
        command.add(audioFile.getUrl());
        logger.debug("Getting file from url " + audioFile.getUrl());

        Process youtubedl;
        try {
            logger.debug("Starting youtube-dl to get file with command: " + command);
            youtubedl = new ProcessBuilder(command).start();
        } catch (IOException e) {
            logger.info("Failed to start youtube-dl! Stacktrace: ", e);
            return DownloadResult.NO_YOUTUBEDL;
        }

        BufferedReader commandOutput = new BufferedReader(new InputStreamReader(youtubedl.getInputStream()));
        BufferedReader commandError = new BufferedReader(new InputStreamReader(youtubedl.getErrorStream()));

        try {
            String currentLine;
            logger.trace("--------------youtube-dl output: ");
            while ((currentLine = commandOutput.readLine()) != null) {
                logger.trace(currentLine);
            }
            logger.trace("--------------youtube-dl output finished");
            logger.trace("--------------youtube-dl error: ");
            while ((currentLine = commandError.readLine()) != null) {
                logger.trace(currentLine);
            }
            logger.trace("--------------youtube-dl error finished");
        } catch (IOException e) {
            logger.info("Could not read youtube-dl output! Stacktrace: ", e);
            //noinspection ResultOfMethodCallIgnored
            new File(listener.getFolder() + Constants.YOUTUBE_DL_DOWNLOAD_FOLDER +
                    audioFile.getFileName()).delete();
            return DownloadResult.NO_OUTPUT;
        }

        audioFile.setDownloaded();
        return DownloadResult.SUCCESS;
    }

    /**
     * Gets a list of AudioFiles from an url.
     * @return A list of AudioFiles from the given url. Only contains more than one
     * if the url is a playlist. Can be empty if an error occurred.
     */
    public static List<AudioFile> getAudioInfo(String url) {
        List<String> command = new ArrayList<>(Config.YOUTUBE_DL_GET_INFO_COMMAND);
        command.add(url);
        logger.debug("Getting titles from url " + url);

        Process youtubedl;
        try {
            logger.debug("Starting youtube-dl to get info with command: " + command);
            youtubedl = new ProcessBuilder(command).start();
        } catch (IOException e) {
            logger.info("Failed to start youtube-dl! Stacktrace: ", e);
            return new ArrayList<>();
        }

        BufferedReader commandOutput = new BufferedReader(new InputStreamReader(youtubedl.getInputStream()));
        BufferedReader commandError = new BufferedReader(new InputStreamReader(youtubedl.getErrorStream()));

        List<AudioFile> results = new ArrayList<>();
        try {
            String currentLine;
            logger.trace("--------------youtube-dl output: ");
            while ((currentLine = commandOutput.readLine()) != null) {
                logger.trace(currentLine);
                if (!currentLine.startsWith("ERROR")) {
                    // Parse JSON output for relevant data
                    JSONObject audioInfo = new JSONObject(currentLine);
                    results.add(new AudioFile(
                            audioInfo.getString("id"),
                            audioInfo.getString("title"),
                            audioInfo.getString("webpage_url")
                    ));
                } else {
                    results.add(new AudioFile());
                }
            }
            logger.trace("--------------youtube-dl output finished");
            logger.trace("--------------youtube-dl error: ");
            while ((currentLine = commandError.readLine()) != null) {
                logger.trace(currentLine);
            }
            logger.trace("--------------youtube-dl error finished");
        } catch (IOException e) {
            logger.info("Could not read youtube-dl output! Stacktrace: ", e);
            return new ArrayList<>();
        }

        return results;
    }

    /**
     * Runs the DownloadManager as a Thread, letting it download the AudioFile specified for the guild. It'll notify the
     * Listener when it's done.
     */
    @Override
    public void run() {
        listener.notifyOfCompletion(getAudioFile(), audioFile);
    }
}
