package com.streambus.commonmodule.bean;

import com.streambus.basemodule.utils.SLog;
import com.streambus.commonmodule.api.DaoHelper;
import com.streambus.commonmodule.utils.NumberUtil;

import java.io.*;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;


/**
 * Created by Administrator on 2017/8/15.
 */

/**
 * 大节目(多季电视剧,多季电影,电视剧,电影)
 */
public class ChannelVodBean implements Serializable {

    private static final String TAG = "ChannelVodBean";

    private static final long serialVersionUID = 1L;

    private String id;

    private String name;

    /*
     *节目类型(多季电视剧,多季电影,电视剧,电影)
     */
    private String type;

    private String description;

    private String img;

    //大海报
    private String bigImg;

    //小栏目 (电视剧,电影)
    private TreeMap<Integer, ChannelVodBean> channelMap;

    private ArrayList<CategoryListBean> catagoryList;

    /**
     * 节目类型，1：普通节目；2：成人节目
     */
    private String proType;

    //电影
    private ArrayList<LinksBean> links;

    //电视剧(每一集多线路)
    private ArrayList<ArrayList<LinksBean>> linkList;
    //电视剧(每一集多线路) linkList->排序
    private TreeMap<Integer, ArrayList<LinksBean>> linksMap;

    private PropsBean props;

    private ArrayList<TagListBean> tagList;

    private float score;

    private ArrayList<RatingsBean> ratings;

    private ArrayList<PictureBean> picturesList;

    //花絮
    private ArrayList<Trailers> trailers;

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    private Long updateTime = 0l;

    private boolean isKids;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

    public ArrayList<LinksBean> getLinks() {
        return links;
    }

    public void setLinks(ArrayList<LinksBean> links) {
        this.links = links;
    }

    public PropsBean getProps() {
        return props;
    }

    public void setProps(PropsBean props) {
        this.props = props;
    }

    public ArrayList<TagListBean> getTagList() {
        return tagList;
    }

    public void setTagList(ArrayList<TagListBean> tagList) {
        this.tagList = tagList;
    }

    public String getProType() {
        return proType;
    }

    public void setProType(String proType) {
        this.proType = proType;
    }

    public TreeMap<Integer, ChannelVodBean> getChannelMap() {
        return channelMap;
    }

    public Map.Entry<Integer, ChannelVodBean> getChannelBySeason(int seasonValue) {
        if (channelMap != null && channelMap.size() > 0) {
            Integer key = new Integer(seasonValue);
            Map.Entry<Integer, ChannelVodBean> entry = channelMap.ceilingEntry(key);
            if (entry == null) {
                entry = channelMap.lowerEntry(key);
            }
            return entry;
        } else {
            return new AbstractMap.SimpleImmutableEntry<>(0, this);
        }
    }

    public void setChannelMap(TreeMap<Integer, ChannelVodBean> channelMap) {
        this.channelMap = channelMap;
    }

    public String getBigImg() {
        return bigImg;
    }

    public void setBigImg(String bigImg) {
        this.bigImg = bigImg;
    }

    public String getType() {
        return type;
    }

    public boolean isKids() {
        return isKids;
    }

    public void setKids(boolean kids) {
        isKids = kids;
    }

    public void setType(String type) {
        this.type = type;
    }


    public ArrayList<CategoryListBean> getCatagoryList() {
        if (catagoryList == null) {
            return new ArrayList<>();
        }
        return catagoryList;
    }

    public void setCatagoryList(ArrayList<CategoryListBean> catagoryList) {
        this.catagoryList = catagoryList;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public ArrayList<RatingsBean> getRatings() {
        if (ratings == null) {
            return new ArrayList<>();
        }
        return ratings;
    }

    public void setRatings(ArrayList<RatingsBean> ratings) {
        this.ratings = ratings;
    }

    public ArrayList<PictureBean> getPicturesList() {
        if (picturesList == null) {
            return new ArrayList<>();
        }
        return picturesList;
    }

    public void setPicturesList(ArrayList<PictureBean> picturesList) {
        this.picturesList = picturesList;
    }

    public ArrayList<ArrayList<LinksBean>> getLinkList() {
        return linkList;
    }

    public void setLinkList(ArrayList<ArrayList<LinksBean>> linkList) {
        this.linkList = linkList;
    }


    public TreeMap<Integer, ArrayList<LinksBean>> getLinkEntryMap() {
        SLog.i(TAG, "getLinkEntryMap,type=" + type + "   linksMap=" + linksMap + "   linkList=" + linkList);
        if (ChannelVodBean.VIDEO_TYPE_TV.equals(type) && linkList == null && links != null) {
            linkList = new ArrayList<>();
            for (LinksBean link : links) {
                ArrayList al = new ArrayList<>();
                al.add(link);
                linkList.add(al);
            }
        }
        if (linksMap == null && linkList != null) {
            linksMap = new TreeMap<>();
            for (int i = 0; i < linkList.size(); i++) {
                int episodeValue = NumberUtil.parseInt(linkList.get(i).get(0).getIndex(), i + 1);
                linksMap.put(episodeValue, linkList.get(i));
            }
        } else if (linkList == null && links != null) {
            linksMap = new TreeMap<>();
            linksMap.put(1, links);
        }
        return linksMap;
    }

    public Map.Entry<Integer, ArrayList<LinksBean>> getLinksByEpisode(int episodeValue) {
        TreeMap<Integer, ArrayList<LinksBean>> linksMap = getLinkEntryMap();
        if (linksMap != null && linksMap.size() > 0) {
            Integer key = new Integer(episodeValue);
            Map.Entry<Integer, ArrayList<LinksBean>> entry = linksMap.ceilingEntry(key);
            if (entry == null) {
                entry = linksMap.lowerEntry(key);
            }
            return entry;
        } else {
            return new AbstractMap.SimpleImmutableEntry<>(0, links);
        }
    }

    public ArrayList<Trailers> getTrailers() {
        return trailers;
    }

    private TreeMap<Integer, Trailers> trailerMap;
    public TreeMap<Integer, Trailers> getTrailerMap() {
        if (trailerMap != null) {
            return trailerMap;
        }
        trailerMap = new TreeMap();
        if (trailers != null) {
            for (int i = 0; i < trailers.size(); i++) {
                trailerMap.put(i + 1, trailers.get(i));
            }
        }
        return trailerMap;
    }

    public void setTrailers(ArrayList<Trailers> trailers) {
        this.trailers = trailers;
    }

    private String typeInfo;// 节目详情 Type:
    private boolean isCc;
    //成人类型
    public static final String ADULT_TYPE = "2";

    //普通类型
    public static final String NORMAL_TYPE = "1";

    public boolean isCc() {
        return isCc;
    }

    public void setCc(boolean cc) {
        isCc = cc;
    }

    //字幕
    private ArrayList<SubtitleBean> subtitleList;

    public String VIDEO_NEW_FLAG = "1";

    //Live直播
    public static String VIDEO_TYPE_LIVE = "1";

    //playBack回放
    public static String VIDEO_TYPE_PLAYBACK = "2";
    //电影
    public static String VIDEO_TYPE_MOVIE = "3";

    //电视剧
    public static String VIDEO_TYPE_TV = "4";

    //系列电影
    public static String VIDEO_TYPE_SERIES_MOVIE = "5";

    //系列电视剧
    public static String VIDEO_TYPE_SERIES_TV = "6";

    //show
    public static String VIDEO_TYPE_SHOW = "7";

    //系列show
    public static String VIDEO_TYPE_SERIES_SHOW = "8";

    //sport
    public static String VIDEO_TYPE_SPORT = "10";

    //专题
    public static String VIDEO_TYPE_TOPIC = "11";

    //自定义mais类型
    public static String VIDEO_TYPE_MAIS = "1001";
    //客户端自定义类型, ROLE=导演、明星、编辑
    public static String VIDEO_TYPE_ROLE = "1002";

    //新版本接口类型(电影和专辑电影)
    public static String CATEGORY_TYPE_MV = "movies_all";

    //kids和系列kids
    public static String CATEGORY_TYPE_KIDS = "anime_all";

    //电视剧和多季电视剧
    public static String CATEGORY_TYPE_TV = "serials_all";

    //show和系列show
    public static String CATEGORY_TYPE_SHOW = "show_all";

    //sport和系列sport
    public static String CATEGORY_TYPE_SPORT = "sport_all";

    //cinema_sd 系列
    public static String CATEGORY_TYPE_CINEAM_SD = "cinema_sd_all";
    public static String CATEGORY_TYPE_NOW_PLAYING = "now_playing_all";
    //成人类型系列,自定义类型，用于区分movies
    public static String CATEGORY_TYPE_ADULT = "adult_all";
    //成人类型系列,自定义类型，用于区分movies
    public static String CATEGORY_TYPE_UNKOWN = "type_unkown";

    public void analyzeCc() {
        Map<String, SubtitleBean> totelList = new HashMap<>();
        if (links != null) {
            for (LinksBean linkBean : links) {
                ArrayList<SubtitleBean> sbtList = linkBean.getSubtitleList();//.getSbtList();
                if (sbtList != null && !sbtList.isEmpty()) {
                    if (sbtList.size() == 1) {
                        SubtitleBean subtitleBean = sbtList.get(0);
                        totelList.put(subtitleBean.getLanguage(), subtitleBean);
                    } else {
                        isCc = true;
                        return;
                    }
                }
                //}
                //}
            }
        }
        if (totelList.size() > 1) {
            isCc = true;
        }
    }

    public boolean hasHis() {
        return DaoHelper.isVodHistory(Long.parseLong(id));
    }

    public String getPoster_H() {
        if (picturesList != null && !picturesList.isEmpty()) {
            for (int i = 0; i < picturesList.size(); i++) {
                SLog.i(TAG, "getPoster_H:  " + picturesList.get(i));
                if (picturesList.get(i) != null && picturesList.get(i).getType().equals(PictureBean.TYPE_POSTER_H)) {
                    return picturesList.get(i).getUrl();
                }
            }
        }
        return img;
    }

    public String getPoster_L() {
        if (picturesList != null && !picturesList.isEmpty()) {
            for (int i = 0; i < picturesList.size(); i++) {
                SLog.i(TAG, "getPoster_H:  " + picturesList.get(i));
                if (picturesList.get(i) != null && picturesList.get(i).getType().equals(PictureBean.TYPE_POSTER_L)) {
                    return picturesList.get(i).getUrl();
                }
            }
        }
        return img;
    }

    public String getRecommend() {
        if (picturesList != null && !picturesList.isEmpty()) {
            for (int i = 0; i < picturesList.size(); i++) {
                SLog.i(TAG, "getPoster_H:  " + picturesList.get(i));
                if (picturesList.get(i) != null && picturesList.get(i).getType().equals(PictureBean.TYPE_RECOMMEND)) {
                    return picturesList.get(i).getUrl();
                }
            }
        }
        return img;
    }

    public String getBackground() {
        String picture = img;
        if (null != picturesList) {
            for (PictureBean pictureBean : picturesList) {
                if (pictureBean.getType().equals(PictureBean.TYPE_BACKGROUND)) {
                     picture = pictureBean.getUrl();
                    break;
                } else if (pictureBean.getType().equals(PictureBean.TYPE_POSTER_L)) {
                     picture = pictureBean.getUrl();
                }
            }
        }
        return picture;
    }


    public String getBanner() {
        if (picturesList != null && !picturesList.isEmpty()) {
            for (int i = 0; i < picturesList.size(); i++) {
                SLog.i(TAG, "getPoster_H:  " + picturesList.get(i));
                if (picturesList.get(i) != null && picturesList.get(i).getType().equals(PictureBean.TYPE_BANNER)) {
                    return picturesList.get(i).getUrl();
                }
            }
        }
        return img;
    }

    @Override
    public String toString() {
        return "ChannelListBean{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", description='" + description + '\'' +
                ", img='" + img + '\'' +
                ", bigImg='" + bigImg + '\'' +
                ", channelMap=" + channelMap +
                ", catagoryList=" + catagoryList +
                ", proType='" + proType + '\'' +
                ", links=" + links +
                ", linkList=" + linkList +
                ", props=" + props +
                ", tagList=" + tagList +
                ", score=" + score +
                ", ratings=" + ratings +
                ", picturesList=" + picturesList +
                ", trailers=" + trailers +
                '}';
    }


    public ChannelVodBean copy() throws Exception {
        ChannelVodBean channelVodBean = null;
        //本对象写入流
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(out);
        oos.writeObject(this);
        //从流中读取
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(out.toByteArray()));
        channelVodBean = (ChannelVodBean) ois.readObject();
        return channelVodBean;
    }

}
