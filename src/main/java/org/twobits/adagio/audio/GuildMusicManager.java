package org.twobits.adagio.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import sx.blah.discord.handle.obj.IGuild;

/**
 * Created by Bab on 10-5-2017.
 */
public class GuildMusicManager {
    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    public GuildMusicManager(AudioPlayerManager manager, IGuild guild) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(guild, player);
    }

    public AudioProvider getAudioProvider() {
        return new AudioProvider(player);
    }
}
