import java.io.BufferedInputStream;
import java.io.FileInputStream;
import javazoom.jl.player.Player;
import javazoom.jl.player.advanced.AdvancedPlayer;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

public class MP3 {
    private String filename;
    private AdvancedPlayer player;
    private int pausedFrame = 0;
    private boolean playing = false;

    public MP3(String filename) {
        this.filename = filename;
        this.close();
    }

    public void close() {
        if (player != null)
            player.close();
    }

    public void changeSong(String filename) {
        this.filename = filename;
        this.close();
    }

    public void stop() {
        if (player != null) {
            player.stop();
            playing = false;
        }
    }

    public void play() {
        try {
            FileInputStream fileInputStream = new FileInputStream(filename);
            BufferedInputStream bufferedInputStream = new BufferedInputStream(fileInputStream);
            player = new AdvancedPlayer(bufferedInputStream);
            player.setPlayBackListener(new PlaybackListener() {
                @Override
                public void playbackStarted(PlaybackEvent event) {
                    setPlaying(true);
                }

                @Override
                public void playbackFinished(PlaybackEvent e) {
                    setPlaying(false);
                }
            });
            playing = true;
        } catch (Exception e) {
            System.out.println(e);
        }

        new Thread() {
            public void run() {
                try {
                    player.play(pausedFrame, Integer.MAX_VALUE);
                } catch (Exception e) {
                    System.out.println(e);
                }
            }

        }.start();
    }

    public void setPlaying(boolean bool){
        playing = bool;
    }
    public void setPausedFrame(int frame) {
        pausedFrame = frame;
    }

    public boolean getPlaying(){
        return playing;
    }
    public int getPausedFrame() {
        return pausedFrame;
    }
}
