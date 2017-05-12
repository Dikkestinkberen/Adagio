package org.twobits.adagio.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.*;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.twobits.adagio.client.AdagioClient;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Bab on 10-5-2017.
 */
public class TrackScheduler implements AudioEventListener {
    @SuppressWarnings("unused")
    private final static Logger logger = LoggerFactory.getLogger(TrackScheduler.class);
    private final IGuild guild;
    private final AudioPlayer player;
    private final BlockingQueue<AudioTrackWithRequest> queue;
    private AudioTrackWithRequest currentTrack;

    public TrackScheduler(IGuild guild, AudioPlayer player) {
        this.guild = guild;
        this.queue = new LinkedBlockingQueue<>();
        this.player = player;
        player.addListener(this);
    }

    @Override
    public void onEvent(AudioEvent event) {
        if(event instanceof PlayerPauseEvent) {
            currentTrack.request.getChannel().sendMessage("Player paused.");
        } else if(event instanceof PlayerResumeEvent) {
            currentTrack.request.getChannel().sendMessage("Player resumed.");
        } else if(event instanceof TrackStartEvent) {
            AdagioClient.sendMessage(currentTrack.request.getChannel(), "Now Playing: " + currentTrack.audioTrack.getInfo().title);
            logger.info("Track started: " + currentTrack.audioTrack.getInfo().title);
        } else if(event instanceof TrackEndEvent) {
            if (queue.isEmpty()) {
                currentTrack = null;
                // Disconnect in a minute if there are still no songs scheduled.
                new java.util.Timer().schedule(
                        new java.util.TimerTask() {
                            @Override
                            public void run() {
                                if (currentTrack == null) {
                                    guild.getConnectedVoiceChannel().leave();
                                    logger.info("Disconnected from voice '" + guild.getConnectedVoiceChannel().getName() + "' for inactivity.");
                                }
                            }
                        },
                        60000
                );
            } else {
                currentTrack = queue.remove();
                player.startTrack(currentTrack.audioTrack, false);
                logger.info("Track ended: " + currentTrack.audioTrack.getInfo().title);
            }
        } else if (event instanceof TrackExceptionEvent) {
            logger.warn("Track Exception in guild: " + guild.getStringID() +
                    ", track: '" + ((TrackExceptionEvent)event).track + "'",
                    ((TrackExceptionEvent)event).exception);
        }
    }

    public boolean hasSong() {
        return currentTrack != null;
    }

    public void queue(IMessage request, AudioTrack audioTrack) {
        if (currentTrack == null) {
            currentTrack = new AudioTrackWithRequest(audioTrack, request);
            player.startTrack(audioTrack, true);
        } else {
            //TODO: Message
            queue.offer(new AudioTrackWithRequest(audioTrack, request));
        }
    }

    public void skip() {
        player.stopTrack();
    }
}
