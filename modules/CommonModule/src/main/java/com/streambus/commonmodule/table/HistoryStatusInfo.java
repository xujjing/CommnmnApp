package com.streambus.commonmodule.table;

import com.google.gson.reflect.TypeToken;
import com.streambus.commonmodule.utils.GsonHelper;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.util.Map;
import java.util.TreeMap;

/**
 * ================================================
 * 作    者：xujianjing
 * 版    本：v1.0
 * 创建日期：2020/12/17
 * 描    述:
 * 修订历史：
 * ================================================
 */
@Entity
public class HistoryStatusInfo{

    @Id
    private Long id; //channelId

    private int progress; // 当前节目播放进度
    private String videoLanguage; //当前播放音频语言
    private String sbtLanguage; //当前播放字幕语言


    private int seasonValue; //当前播放季数
    private int EpisodeValue;//当前播放集数

    private int lookEpisodeVersion = LOOK_EPISODE_CURRENT; //lookEpisodeString版本
    private String lookEpisodeString; //季集播放状态 （目前记录了播放过的剧集和各集播放进度）

    @Transient private static final int LOOK_EPISODE_STRING_V1 = 1;
    @Transient private static final int LOOK_EPISODE_CURRENT = LOOK_EPISODE_STRING_V1;
    @Transient private Map<Integer, Map<Integer, Integer>> lookEpisodeMap = new TreeMap<>();


    private int trailerValue;
    private int lookTrailerVersion = LOOK_TRAILER_CURRENT;
    private String lookTrailerString; //花絮播放状态
    @Transient private static final int LOOK_TRAILER_STRING_V1 = 1;
    @Transient private static final int LOOK_TRAILER_CURRENT = LOOK_TRAILER_STRING_V1;
    @Transient private Map<Integer, Integer> lookTrailerMap = new TreeMap<>();


    public void resetPlayStatus() {
        progress = 0;
        seasonValue = 0;
        EpisodeValue = 0;
        lookEpisodeString = null;
        lookEpisodeVersion = LOOK_EPISODE_CURRENT;
        lookEpisodeMap = new TreeMap<>();
    }

    public void resetTrailerStatus() {
        trailerValue = 0;
        lookTrailerString = null;
        lookTrailerVersion = LOOK_TRAILER_CURRENT;
        lookTrailerMap = new TreeMap<>();
    }

    @Generated(hash = 1163225733)
    public HistoryStatusInfo(Long id, int progress, String videoLanguage, String sbtLanguage, int seasonValue, int EpisodeValue, int lookEpisodeVersion,
            String lookEpisodeString, int trailerValue, int lookTrailerVersion, String lookTrailerString) {
        this.id = id;
        this.progress = progress;
        this.videoLanguage = videoLanguage;
        this.sbtLanguage = sbtLanguage;
        this.seasonValue = seasonValue;
        this.EpisodeValue = EpisodeValue;
        this.lookEpisodeVersion = lookEpisodeVersion;
        this.lookEpisodeString = lookEpisodeString;
        this.trailerValue = trailerValue;
        this.lookTrailerVersion = lookTrailerVersion;
        this.lookTrailerString = lookTrailerString;
    }

    @Generated(hash = 1917208258)
    public HistoryStatusInfo() {
    }


    public String getLookEpisodeString() {
        return lookEpisodeString;
    }

    public void setLookEpisodeString(String lookEpisodeString) {
        this.lookEpisodeString = lookEpisodeString;
    }

    public void toTypeLookEpisodeMap() {
        try {
            lookEpisodeMap = GsonHelper.toType(lookEpisodeString, new TypeToken<TreeMap<Integer, TreeMap<Integer, Integer>>>(){}.getType());
        } catch (Exception e) {
        }
        if (lookEpisodeMap == null) {
            lookEpisodeMap = new TreeMap<>();
        }
        lookEpisodeVersion = LOOK_EPISODE_CURRENT;
    }

    public void toJsonLookEpisodeString() {
        lookEpisodeString = GsonHelper.toJson(lookEpisodeMap);
    }


    public void toTypeLookTrailerMap() {
        try {
            lookTrailerMap = GsonHelper.toType(lookTrailerString, new TypeToken<TreeMap<Integer, Integer>>(){}.getType());
        } catch (Exception e) {
        }
        if (lookTrailerMap == null) {
            lookTrailerMap = new TreeMap<>();
        }
        lookTrailerVersion = LOOK_TRAILER_CURRENT;
    }

    public void toJsonLookTrailerString() {
        lookTrailerString = GsonHelper.toJson(lookTrailerMap);
    }

    public boolean hadLooked() {
        if (progress > 0) {
            return true;
        }
        if (seasonValue > 1 || EpisodeValue > 1) {
            return true;
        }
        return false;
    }

    public int lookAtEpisode() {
        Map<Integer, Integer> map = lookEpisodeMap.get(seasonValue);
        if (map == null) {
            map = new TreeMap<>();
            lookEpisodeMap.put(seasonValue, map);
        }
        Integer progress = map.get(EpisodeValue);
        if (progress == null){
            progress = new Integer(0);
            map.put(EpisodeValue, progress);
        }
        return this.progress = progress.intValue();
    }

    public void saveLookEpisodeProgress(int progress) {
        Map<Integer, Integer> map = lookEpisodeMap.get(seasonValue);
        if (map == null) {
            map = new TreeMap<>();
            lookEpisodeMap.put(seasonValue, map);
        }
        map.put(EpisodeValue, progress);
        this.progress = progress;
    }

    public Map<Integer, Integer> getLookEpisodes(int season) {
        Map<Integer, Integer> map = lookEpisodeMap.get(season);
        if (map == null) {
            map = new TreeMap<>();
            lookEpisodeMap.put(season, map);
        }
        return map;
    }

    public Map<Integer, Integer> getLookTrailers() {
        return lookTrailerMap;
    }

    public int lookAtTrailer(int trailerValue) {
        this.trailerValue = trailerValue;
        Integer progress = lookTrailerMap.get(trailerValue);
        if (progress == null) {
            progress = 0;
            lookTrailerMap.put(trailerValue, progress);
        }
        return progress;
    }

    public void saveLookTrailerProgress(int progress) {
        lookTrailerMap.put(trailerValue, progress);
        this.progress = progress;
    }

    public int getTrailerValue() {
        return trailerValue;
    }

    public void setTrailerValue(int trailerValue) {
        this.trailerValue = trailerValue;
    }

    public int getLookEpisodeVersion() {
        return lookEpisodeVersion;
    }

    public void setLookEpisodeVersion(int lookEpisodeVersion) {
        this.lookEpisodeVersion = lookEpisodeVersion;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }

    public int getSeasonValue() {
        return seasonValue;
    }

    public void setSeasonValue(int seasonValue) {
        this.seasonValue = seasonValue;
    }

    public int getEpisodeValue() {
        return EpisodeValue;
    }

    public void setEpisodeValue(int episodeValue) {
        this.EpisodeValue = episodeValue;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getVideoLanguage() {
        return this.videoLanguage;
    }

    public void setVideoLanguage(String videoLanguage) {
        this.videoLanguage = videoLanguage;
    }

    public String getSbtLanguage() {
        return this.sbtLanguage;
    }

    public void setSbtLanguage(String sbtLanguage) {
        this.sbtLanguage = sbtLanguage;
    }

    public int getLookTrailerVersion() {
        return this.lookTrailerVersion;
    }

    public void setLookTrailerVersion(int lookTrailerVersion) {
        this.lookTrailerVersion = lookTrailerVersion;
    }

    public String getLookTrailerString() {
        return this.lookTrailerString;
    }

    public void setLookTrailerString(String lookTrailerString) {
        this.lookTrailerString = lookTrailerString;
    }

}
