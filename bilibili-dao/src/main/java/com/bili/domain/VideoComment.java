package com.bili.domain;

import java.util.Date;
import java.util.List;

public class VideoComment {
    private Long id;
    private Long videoId;
    private Long userId;
    private Long replyUserId;
    private String text;
    private Long rootId;
    private Date createTime;
    private Date updateTime;
    private List<VideoComment> childComment;
    private UserInfo userInfo;
    private UserInfo replyTargetUserInfo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }

    public List<VideoComment> getChildComment() {
        return childComment;
    }

    public void setChildComment(List<VideoComment> childComment) {
        this.childComment = childComment;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public UserInfo getReplyTargetUserInfo() {
        return replyTargetUserInfo;
    }

    public void setReplyTargetUserInfo(UserInfo replyTargetUserInfo) {
        this.replyTargetUserInfo = replyTargetUserInfo;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getReplyUserId() {
        return replyUserId;
    }

    public void setReplyUserId(Long replyUserId) {
        this.replyUserId = replyUserId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getRootId() {
        return rootId;
    }

    public void setRootId(Long rootId) {
        this.rootId = rootId;
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
