package com.sukaiyi.bandwagonvps.bean;

import java.sql.Date;

/**
 * Created by sukaiyi on 2017/06/02.
 */

public class HostInfoParser {

    private HostInfo mInfo;

    public HostInfoParser(HostInfo info) {
        this.mInfo = info;
    }

    public String getVe_disk_quota_gb() {
        return this.mInfo.getVe_disk_quota_gb();
    }

    public String getVe_status() {
        return this.mInfo.getVe_status();
    }

    public int getVe_used_disk_space_b() {
        return this.mInfo.getVe_used_disk_space_b();
    }

    public String getVe_mac1() {
        return this.mInfo.getVe_mac1();
    }

    public int getSsh_port() {
        return this.mInfo.getSsh_port();
    }

    public String getNode_ip() {
        return this.mInfo.getNode_ip();
    }

    public String getOs() {
        return this.mInfo.getOs();
    }

    public String getNode_location() {
        return this.mInfo.getNode_location();
    }

    public int getPlan_max_ipv6s() {
        return this.mInfo.getPlan_max_ipv6s();
    }

    public String getIp_addresses() {
        StringBuffer sb = new StringBuffer();
        for (String ip : this.mInfo.getIp_addresses()) {
            sb.append(ip);
            sb.append("\n");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }

    public boolean getLocation_ipv6_ready() {
        return this.mInfo.getLocation_ipv6_ready();
    }

    public String getIs_cpu_throttled() {
        return this.mInfo.getIs_cpu_throttled();
    }

    public long getPlan_ram() {
        return this.mInfo.getPlan_ram();
    }

    public boolean getSuspended() {
        return this.mInfo.getSuspended();
    }

    public long getData_counter() {
        return this.mInfo.getData_counter();
    }

    public String getVm_type() {
        return this.mInfo.getVm_type();
    }

    public String getHostname() {
        return this.mInfo.getHostname();
    }

    public String getData_next_reset() {
        return new Date(mInfo.getData_next_reset() * 1000l).toString();
    }

    public long getPlan_monthly_data() {
        return this.mInfo.getPlan_monthly_data();
    }

    public boolean getRdns_api_available() {
        return this.mInfo.getRdns_api_available();
    }

    public String getNode_alias() {
        return this.mInfo.getNode_alias();
    }

    public long getPlan_swap() {
        return this.mInfo.getPlan_swap();
    }

    public long getPlan_disk() {
        return this.mInfo.getPlan_disk();
    }

    public String getPlan() {
        return this.mInfo.getPlan();
    }

    public String getEmail() {
        return this.mInfo.getEmail();
    }

    public int getError() {
        return mInfo.getError();
    }

    public String getCpu_load() {
        return mInfo.getVz_status().getNproc() +
                " processes LA:" + mInfo.getVz_status().getLoad_average();
    }

    public String getStatus() {
        if (getVm_type().equals("ovz")) {
            return mInfo.getVz_status().getStatus();
        } else {
            return mInfo.getVe_status();
        }
    }

    public long getUsage_ram() {
        if (mInfo.getVz_status() == null) {
            return -1;
        }
        if (mInfo.getVz_status().getOomguarpages().equals("-"))
            mInfo.getVz_status().setOomguarpages("0");
        return java.lang.Long.parseLong(mInfo.getVz_status().getOomguarpages()) * 4 * 1024;
    }

    public long getUsage_swap() {
        if (mInfo.getVz_status() == null) {
            return -1;
        }
        if (mInfo.getVz_status().getSwappages().equals("-"))
            mInfo.getVz_status().setSwappages("0");
        return java.lang.Long.parseLong(mInfo.getVz_status().getSwappages()) * 4 * 1024;
    }

    public long getUsage_disk() {
        if (mInfo.getVz_quota() == null) {
            return -1;
        }
        if (mInfo.getVz_quota().getOccupied_kb().equals("-"))
            mInfo.getVz_quota().setOccupied_kb("0");
        return java.lang.Long.parseLong(mInfo.getVz_quota().getOccupied_kb()) * 1024;
    }



}
