package org.twobits.adagio.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import sx.blah.discord.handle.obj.IMessage;

/**
 * Created by Bab on 10-5-2017.
 */
public class AudioTrackWithRequest {
    public final AudioTrack audioTrack;
    public final IMessage request;

    public AudioTrackWithRequest(AudioTrack audioTrack, IMessage request) {
        this.audioTrack = audioTrack;
        this.request = request;
    }
}
