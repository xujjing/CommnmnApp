package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/8/15.
 * 扩展属性
 */

public class PropsBean implements Serializable {

    private String release_time;
    private String runtime;
    private String language;
    private String country;
    private String director;
    private String score;
    private String stars;
    private String type;

    private String new_flag;
    private String keyword;
    private String season_index;
    private String serial_amount;
    private String Audio;

    public boolean isMultiLanguage() {
        if (language != null) {
            if (language.split("/").length > 1) {
                return true;
            }
        }
        if (Audio != null) {
            if (Audio.split("/").length > 1) {
                return true;
            }
        }
        return false;
    }

    public String getRelease_time() {
        return release_time;
    }

    public void setRelease_time(String release_time) {
        this.release_time = release_time;
    }

    public String getRuntime() {
        return runtime;
    }

    public void setRuntime(String runtime) {
        this.runtime = runtime;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDirector() {
        return director;
    }

    public void setDirector(String director) {
        this.director = director;
    }


    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getStars() {
        return stars;
    }

    public void setStars(String stars) {
        this.stars = stars;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNew_flag() {
        return new_flag;
    }

    public void setNew_flag(String new_flag) {
        this.new_flag = new_flag;
    }

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public String getSeason_index() {
        return season_index;
    }

    public void setSeason_index(String season_index) {
        this.season_index = season_index;
    }

    public String getSerial_amount() {
        return serial_amount == null ? "" : serial_amount;
    }

    public void setSerial_amount(String serial_amount) {
        this.serial_amount = serial_amount;
    }

    public String getAudio() {
        return Audio;
    }

    public void setAudio(String audio) {
        Audio = audio;
    }

    public boolean isNew() {
        return "1".equals(new_flag);
    }

    @Override
    public String toString() {
        return "PropsBean{" +
                "release_time='" + release_time + '\'' +
                ", runtime='" + runtime + '\'' +
                ", language='" + language + '\'' +
                ", country='" + country + '\'' +
                ", director='" + director + '\'' +
                ", score='" + score + '\'' +
                ", stars='" + stars + '\'' +
                ", type='" + type + '\'' +
                ", new_flag='" + new_flag + '\'' +
                ", keyword='" + keyword + '\'' +
                ", season_index='" + season_index + '\'' +
                ", serial_amount='" + serial_amount + '\'' +
                ", Audio='" + Audio + '\'' +
                '}';
    }

}
