package com.streambus.commonmodule.bean;

import com.streambus.commonmodule.utils.LanguageUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huanglu on 2017/5/22.
 */


public class LinksBean implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final String MEDIA_TYPE_LIVE = "LIVE";
    public static final String MEDIA_TYPE_PLAYBACK = "PLAYBACK";

    public static final String TAG_TYPE_4K = "4K";
    public static final String TAG_TYPE_3D = "3D";

    private String mediaType;

    private String vodid;

    private String filmname;

    private String lpic;

    private String format;

    private String type;

    private String httpurl; //直连地址

    private String index;

    private String filmid;

    private String server; //p2p 代理地址

    private String description;

    private String codec;

    private List<String> language;
    //字幕
    private ArrayList<SubtitleBean> subtitleList;

    //校验规则 版本
    private String protocolVersion;
    private static final String PROTOCOL_DEFAULT_VERSION = "1.0";

    //用于下载后本地播放
    private String localPath;

    public int protocolVersion() {
        try {
            return Integer.parseInt(protocolVersion.substring(0, 1));
        } catch (Exception ignore) {
        }
        return 1;
    }

    public void setProtocolVersion(String protocolVersion) {
        this.protocolVersion = protocolVersion;
    }

    public void setLocalPath(String localPath) {
        this.localPath = localPath;
    }

    public String getLocalPath() {
        return localPath;
    }

    public List<String> getLanguage() {
        return language;
    }

    public void setLanguage(List<String> language) {
        this.language = language;
    }

    private boolean isDownload = false;

    public String getVodid() {
        return vodid;
    }

    public void setVodid(String vodid) {
        this.vodid = vodid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFilmname() {
        return filmname;
    }

    public void setFilmname(String filmname) {
        this.filmname = filmname;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getHttpurl() {
        return httpurl;
    }

    public void setHttpurl(String httpurl) {
        this.httpurl = httpurl;
    }

    public String getLpic() {
        return lpic;
    }

    public void setLpic(String lpic) {
        this.lpic = lpic;
    }

    public String getIndex() {
        return index;
    }

    public void setIndex(String index) {
        this.index = index;
    }

    public String getFilmid() {
        return filmid;
    }

    public void setFilmid(String filmid) {
        this.filmid = filmid;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDownload() {
        return isDownload;
    }

    public void setDownload(boolean download) {
        isDownload = download;
    }

    public String getCodec() {
        return codec == null ? "" : codec;
    }

    public void setCodec(String codec) {
        this.codec = codec;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public ArrayList<SubtitleBean> getSubtitleList() {
        return subtitleList;
    }

    public void setSubtitleList(ArrayList<SubtitleBean> subtitleList) {
        this.subtitleList = subtitleList;
    }

    public List<String> getSubtLanguageList() {
        ArrayList<String> subtLanguage = new ArrayList<>();
        if (subtitleList != null) {
            for (SubtitleBean bean  : subtitleList) {
                subtLanguage.add(bean.getLanguage());
            }
        }
        return subtLanguage;
    }

    public SubtitleBean findSubtitle(String language) {
        if (subtitleList != null && language != null) {
            for (SubtitleBean bean : subtitleList) {
                if (LanguageUtils.equalsLanguage(language, bean.getLanguage())) {
                    return bean;
                }
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "LinksBean{" +
                "mediaType='" + mediaType + '\'' +
                ", vodid='" + vodid + '\'' +
                ", filmname='" + filmname + '\'' +
                ", format='" + format + '\'' +
                ", type='" + type + '\'' +
                ", httpurl='" + httpurl + '\'' +
                ", lpic='" + lpic + '\'' +
                ", index='" + index + '\'' +
                ", filmid='" + filmid + '\'' +
                ", server='" + server + '\'' +
                ", description='" + description + '\'' +
                ", codec='" + codec + '\'' +
                ", language=" + language +
                ", sbtList=" + subtitleList +
                ", isDownload=" + isDownload +
                '}';
    }

}
