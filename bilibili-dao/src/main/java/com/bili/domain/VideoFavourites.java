package com.bili.domain;

import java.util.Date;

public class VideoFavourites {
    private Long id;
    private Long userId;
    private Long videoId;
    private Long favouriteGroupId;
    private Date createTime;
    private Date updateTime;

    public Long getFavouriteGroupId() {
        return favouriteGroupId;
    }

    public void setFavouriteGroupId(Long favouriteGroupId) {
        this.favouriteGroupId = favouriteGroupId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }
}
