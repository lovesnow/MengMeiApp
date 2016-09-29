package com.evilbeast.meizi.entity.meizi;

import java.io.Serializable;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Author: sumary
 */
public class MeiZiGroup extends RealmObject implements Serializable {
    @PrimaryKey
    private String imageUrl;
    private int groupId;

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
