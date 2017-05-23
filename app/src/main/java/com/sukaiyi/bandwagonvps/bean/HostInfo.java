package com.sukaiyi.bandwagonvps.bean;

public class HostInfo implements java.io.Serializable {
    private static final long serialVersionUID = -4468241521556421910L;
    private String node_ip;
    private String os;
    private String node_location;
    private int plan_max_ipv6s;
    private String[] ip_addresses;
    private boolean location_ipv6_ready;
    private int error;
    private int plan_ram;
    private boolean suspended;
    private long data_counter;
    private String vm_type;
    private String hostname;
    private int data_next_reset;
    private long plan_monthly_data;
    private boolean rdns_api_available;
    private String node_alias;
    private int plan_swap;
    private long plan_disk;
    private String plan;
    private String email;

    public String getNode_ip() {
        return this.node_ip;
    }

    public void setNode_ip(String node_ip) {
        this.node_ip = node_ip;
    }

    public String getOs() {
        return this.os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getNode_location() {
        return this.node_location;
    }

    public void setNode_location(String node_location) {
        this.node_location = node_location;
    }

    public int getPlan_max_ipv6s() {
        return this.plan_max_ipv6s;
    }

    public void setPlan_max_ipv6s(int plan_max_ipv6s) {
        this.plan_max_ipv6s = plan_max_ipv6s;
    }

    public String[] getIp_addresses() {
        return this.ip_addresses;
    }

    public void setIp_addresses(String[] ip_addresses) {
        this.ip_addresses = ip_addresses;
    }

    public boolean getLocation_ipv6_ready() {
        return this.location_ipv6_ready;
    }

    public void setLocation_ipv6_ready(boolean location_ipv6_ready) {
        this.location_ipv6_ready = location_ipv6_ready;
    }

    public int getError() {
        return this.error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getPlan_ram() {
        return this.plan_ram;
    }

    public void setPlan_ram(int plan_ram) {
        this.plan_ram = plan_ram;
    }

    public boolean getSuspended() {
        return this.suspended;
    }

    public void setSuspended(boolean suspended) {
        this.suspended = suspended;
    }

    public long getData_counter() {
        return this.data_counter;
    }

    public void setData_counter(long data_counter) {
        this.data_counter = data_counter;
    }

    public String getVm_type() {
        return this.vm_type;
    }

    public void setVm_type(String vm_type) {
        this.vm_type = vm_type;
    }

    public String getHostname() {
        return this.hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public int getData_next_reset() {
        return this.data_next_reset;
    }

    public void setData_next_reset(int data_next_reset) {
        this.data_next_reset = data_next_reset;
    }

    public long getPlan_monthly_data() {
        return this.plan_monthly_data;
    }

    public void setPlan_monthly_data(long plan_monthly_data) {
        this.plan_monthly_data = plan_monthly_data;
    }

    public boolean getRdns_api_available() {
        return this.rdns_api_available;
    }

    public void setRdns_api_available(boolean rdns_api_available) {
        this.rdns_api_available = rdns_api_available;
    }

    public String getNode_alias() {
        return this.node_alias;
    }

    public void setNode_alias(String node_alias) {
        this.node_alias = node_alias;
    }

    public int getPlan_swap() {
        return this.plan_swap;
    }

    public void setPlan_swap(int plan_swap) {
        this.plan_swap = plan_swap;
    }

    public long getPlan_disk() {
        return this.plan_disk;
    }

    public void setPlan_disk(long plan_disk) {
        this.plan_disk = plan_disk;
    }

    public String getPlan() {
        return this.plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
