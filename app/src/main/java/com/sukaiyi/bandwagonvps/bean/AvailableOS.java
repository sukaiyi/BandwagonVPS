package com.sukaiyi.bandwagonvps.bean;

public class AvailableOS {
    private String installed;
    private String[] templates;
    private int error;

    public String getInstalled() {
        return this.installed;
    }

    public void setInstalled(String installed) {
        this.installed = installed;
    }

    public String[] getTemplates() {
        return this.templates;
    }

    public void setTemplates(String[] templates) {
        this.templates = templates;
    }

    public int getError() {
        return this.error;
    }

    public void setError(int error) {
        this.error = error;
    }
}
