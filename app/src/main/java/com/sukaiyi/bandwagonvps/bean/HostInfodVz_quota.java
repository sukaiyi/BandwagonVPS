package com.sukaiyi.bandwagonvps.bean;

public class HostInfodVz_quota implements java.io.Serializable {
    private static final long serialVersionUID = 4558473108786481258L;
    private String softlimit_kb;
    private String softlimit_inodes;
    private String hardlimit_inodes;
    private String hardlimit_kb;
    private String occupied_inodes;
    private String occupied_kb;

    public String getSoftlimit_kb() {
        return this.softlimit_kb;
    }

    public void setSoftlimit_kb(String softlimit_kb) {
        this.softlimit_kb = softlimit_kb;
    }

    public String getSoftlimit_inodes() {
        return this.softlimit_inodes;
    }

    public void setSoftlimit_inodes(String softlimit_inodes) {
        this.softlimit_inodes = softlimit_inodes;
    }

    public String getHardlimit_inodes() {
        return this.hardlimit_inodes;
    }

    public void setHardlimit_inodes(String hardlimit_inodes) {
        this.hardlimit_inodes = hardlimit_inodes;
    }

    public String getHardlimit_kb() {
        return this.hardlimit_kb;
    }

    public void setHardlimit_kb(String hardlimit_kb) {
        this.hardlimit_kb = hardlimit_kb;
    }

    public String getOccupied_inodes() {
        return this.occupied_inodes;
    }

    public void setOccupied_inodes(String occupied_inodes) {
        this.occupied_inodes = occupied_inodes;
    }

    public String getOccupied_kb() {
        return this.occupied_kb;
    }

    public void setOccupied_kb(String occupied_kb) {
        this.occupied_kb = occupied_kb;
    }
}
