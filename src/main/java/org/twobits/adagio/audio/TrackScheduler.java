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
            event.player.setPaused(true);
        } else if(event instanceof PlayerResumeEvent) {
            event.player.setPaused(false);
        } else if(event instanceof TrackStartEvent) {
            AdagioClient.sendMessage(currentTrack.request.getChannel(), "Now Playing: " + currentTrack.audioTrack.getInfo().title);
            logger.info("Track started: " + currentTrack.audioTrack.getInfo().title);
        } else if(event instanceof TrackEndEvent) {
            currentTrack = queue.remove();
            player.startTrack(currentTrack.audioTrack, false);
            logger.info("Track ended: " + currentTrack.audioTrack.getInfo().title);
        } else if(event instanceof TrackExceptionEvent) {
            logger.warn("Track Exception in guild: " + guild.getStringID() +
                    ", track: '" + ((TrackExceptionEvent)event).track + "'",
                    ((TrackExceptionEvent)event).exception);
        }
    }

    public void queue(IMessage request, AudioTrack audioTrack) {
        if (!player.startTrack(audioTrack, true)) {
            //TODO: Message
            queue.offer(new AudioTrackWithRequest(audioTrack, request));
        }
    }
}
