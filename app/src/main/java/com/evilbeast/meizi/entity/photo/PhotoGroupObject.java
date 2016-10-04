package com.evilbeast.meizi.entity.photo;

import java.io.Serializable;

import io.realm.RealmObject;

/**
 * Author: sumary
 */
public class PhotoGroupObject extends RealmObject implements Serializable {

    private String imageUrl;
    private String title;
    private String type;
    private String module;
    private  int groupId;
    private int position;

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }



    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
