package tv.danmaku.ijk.media.widget.media.trackbean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Administrator on 2017/12/29.
 */

public class VideoTrackBean implements Serializable {
    private int trackIndex;
    private String definitionType;
    private ArrayList<AudioTrackBean> audioTrackBeans;

    public int getTrackIndex() {
        return trackIndex;
    }

    public void setTrackIndex(int trackIndex) {
        this.trackIndex = trackIndex;
    }

    public String getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(String definitionType) {
        this.definitionType = definitionType;
    }

    public ArrayList<AudioTrackBean> getAudioTrackBeans() {
        return audioTrackBeans;
    }

    public void setAudioTrackBeans(ArrayList<AudioTrackBean> audioTrackBeans) {
        this.audioTrackBeans = audioTrackBeans;
    }

    @Override
    public String toString() {
        return "VideoTrackBean{" +
                "trackIndex=" + trackIndex +
                ", definitionType='" + definitionType + '\'' +
                ", audioTrackBeans=" + audioTrackBeans +
                '}';
    }
}
