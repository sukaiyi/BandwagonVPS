package com.sukaiyi.bandwagonvps.bean;

public class Snapshot {
    private String fileName;
    private String os;
    private String size;
    private String downloadLink;
    private boolean sticky;
    private String description;
    private long uncompressed;
    private int purgesIn;
    private String md5;

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getSize() {
        return this.size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDownloadLink() {
        return this.downloadLink;
    }

    public void setDownloadLink(String downloadLink) {
        this.downloadLink = downloadLink;
    }

    public boolean getSticky() {
        return this.sticky;
    }

    public void setSticky(boolean sticky) {
        this.sticky = sticky;
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getUncompressed() {
        return this.uncompressed;
    }

    public void setUncompressed(long uncompressed) {
        this.uncompressed = uncompressed;
    }

    public int getPurgesIn() {
        return this.purgesIn;
    }

    public void setPurgesIn(int purgesIn) {
        this.purgesIn = purgesIn;
    }

    public String getMd5() {
        return this.md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }
}
