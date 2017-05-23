package com.sukaiyi.bandwagonvps.bean;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by sukaiyi on 2017/05/13.
 */

public class Host implements Serializable{
    private String name;
    private String ID;
    private String apiKey;

    public Host() {
    }

    public Host(String name, String ID, String apiKey) {
        this.name = name;
        this.ID = ID;
        this.apiKey = apiKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    @Override
    public String toString() {
        Gson gson = new Gson();
        return gson.toJson(this);
    }
}
