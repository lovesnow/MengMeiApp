package com.evilbeast.meizi.entity.fuli;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Author: sumary
 */
public class FuliItemObject extends RealmObject implements Serializable {
    private String title;
    private String imageUrl;
    private int groupId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }
}
