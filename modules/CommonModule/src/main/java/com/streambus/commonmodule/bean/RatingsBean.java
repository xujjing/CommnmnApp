package com.streambus.commonmodule.bean;

import java.io.Serializable;

/**
 * Created by LiZhiGang on 2018/6/15.
 */

public class RatingsBean implements Serializable {

    private int rating;
    private int ratingTime;

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public int getRatingTime() {
        return ratingTime;
    }

    public void setRatingTime(int ratingTime) {
        this.ratingTime = ratingTime;
    }

    @Override
    public String toString() {
        return "RatingsBean{" +
                "rating=" + rating +
                ", ratingTime=" + ratingTime +
                '}';
    }

}
