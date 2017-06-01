package com.sukaiyi.bandwagonvps.bean;

import com.chad.library.adapter.base.entity.MultiItemEntity;

/**
 * Created by sukaiyi on 2017/05/29.
 */

public class Shell implements MultiItemEntity {
    public static final int TYPE_REQUEST = 0;
    public static final int TYPE_RESPONSE = 1;

    private int type = 0;
    private String message;
    private String currentPath;

    private boolean isOver = false;

    public Shell() {
    }

    public Shell(int type, String message, String currentPath, boolean isOver) {
        this.type = type;
        this.message = message;
        this.currentPath = currentPath;
        this.isOver = isOver;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    @Override
    public int getItemType() {
        return type;
    }

    public boolean isOver() {
        return isOver;
    }

    public void setOver(boolean over) {
        isOver = over;
    }
}
