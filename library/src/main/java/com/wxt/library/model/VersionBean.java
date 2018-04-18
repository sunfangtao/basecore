package com.wxt.library.model;

import com.wxt.library.sql.model.DBVO;

public class VersionBean extends DBVO {

    @Override
    public String findKeyForVO() {
        return "versionUrl";
    }

    private String versionUrl;
    private String versionCode;
    private String title;
    private String content;
    private String isForce;

    public String getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(String versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionUrl() {
        return versionUrl;
    }

    public void setVersionUrl(String versionUrl) {
        this.versionUrl = versionUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getIsForce() {
        return isForce;
    }

    public void setIsForce(String isForce) {
        this.isForce = isForce;
    }
}