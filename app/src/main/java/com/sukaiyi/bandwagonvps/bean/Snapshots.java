package com.sukaiyi.bandwagonvps.bean;

public class Snapshots {
    private Snapshot[] snapshots;
    private int error;

    public Snapshot[] getSnapshots() {
        return this.snapshots;
    }

    public void setSnapshots(Snapshot[] snapshots) {
        this.snapshots = snapshots;
    }

    public int getError() {
        return this.error;
    }

    public void setError(int error) {
        this.error = error;
    }
}
