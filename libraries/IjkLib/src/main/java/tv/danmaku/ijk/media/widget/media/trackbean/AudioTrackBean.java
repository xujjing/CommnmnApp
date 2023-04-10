package tv.danmaku.ijk.media.widget.media.trackbean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/12/29.
 */

public class AudioTrackBean implements Serializable {
    private int trackIndex;
    private String language;

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return "AudioTrackBean{" +
                "trackIndex=" + trackIndex +
                ", language='" + language + '\'' +
                '}';
    }
}
