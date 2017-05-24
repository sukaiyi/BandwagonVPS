package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.HostInfo;
import com.sukaiyi.bandwagonvps.net.ApiGate;
import com.sukaiyi.bandwagonvps.utils.Switch;
import com.sukaiyi.bandwagonvps.view.HostProgressItemView;
import com.sukaiyi.bandwagonvps.view.HostSimpleItemView;

import org.json.JSONObject;

import java.sql.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

public class HostDetailActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private Host mHost;
    private HostInfo mInfo;

    @BindView(R.id.root_view)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.host_name)
    TextView mHostNameView;
    @BindView(R.id.host_id)
    TextView mHostIDView;
    @BindView(R.id.plan)
    TextView mPlanView;
    @BindView(R.id.email)
    TextView mEmailView;

    @BindView(R.id.node_location)
    HostSimpleItemView mHostLocation;

    @BindView(R.id.node_os)
    HostSimpleItemView mHostOS;

    @BindView(R.id.node_ip)
    HostSimpleItemView mHostIP;

    @BindView(R.id.node_ssh_port)
    HostSimpleItemView mHostSSHPort;

    @BindView(R.id.node_status)
    HostSimpleItemView mHostStatus;

    @BindView(R.id.node_cpu)
    HostSimpleItemView mHostCpu;
    @BindView(R.id.bandwidth_usage)
    HostProgressItemView mBandwidthUsage;

    @BindView(R.id.ram_usage)
    HostProgressItemView mRamUsage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHost = (Host) intent.getSerializableExtra("host");
        if (mHost == null) {
            this.finish();
            return;
        }
        setContentView(R.layout.activity_host_detail);
        ButterKnife.bind(this);

        loadCache();

        getHostInfo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                HostInfo info = gson.fromJson(response.toString(), HostInfo.class);
                Logger.d(response);
                if (info.getError() == 0) {
                    mInfo = info;
                    bindView();
                } else {
                    error(info.getError());
                }
            }
        });
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void loadCache() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String info = sharedPreferences.getString(mHost.getID(),"");
        if(TextUtils.isEmpty(info)){
            return;
        }
        mInfo = new Gson().fromJson(info,HostInfo.class);
        bindView();
    }

    private void error(int errorCode) {
        Snackbar.make(mHostNameView, "错误:" + errorCode, Snackbar.LENGTH_LONG).show();
    }

    private void bindView() {
        mHostNameView.setText(mInfo.getHostname());
        mHostIDView.setText(mHost.getID());
        mPlanView.setText(mInfo.getPlan());
        mEmailView.setText(mInfo.getEmail());

        mHostLocation.setValue(mInfo.getNode_location());
        mHostOS.setValue(mInfo.getOs());

        String[] ips = mInfo.getIp_addresses();
        StringBuilder sb = new StringBuilder();
        for (String ip : ips) {
            sb.append(ip);
            sb.append("\n");
        }
        if (sb.length() > 0) sb.deleteCharAt(sb.length() - 1);
        mHostIP.setValue(sb.toString());
        mHostSSHPort.setValue(mInfo.getSsh_port() + "");
        mHostStatus.setValue(mInfo.getVz_status().getStatus());
        mHostCpu.setValue(mInfo.getVz_status().getNproc() +
                " processes LA:" + mInfo.getVz_status().getLoad_average());

        mBandwidthUsage.setValue(Switch.b2Any(mInfo.getData_counter()) + "/" + Switch.b2Any(mInfo.getPlan_monthly_data()));
        mBandwidthUsage.setProgress((int) (mInfo.getData_counter() * 100 / mInfo.getPlan_monthly_data()));
        mBandwidthUsage.setTips("Reset:" + new Date(mInfo.getData_next_reset() * 1000l).toString());
        String ramUsage = mInfo.getVz_status().getKmemsize();
        if(ramUsage.equals("-")){
            ramUsage = "0";
        }
        mRamUsage.setValue(Switch.b2Any(Integer.parseInt(ramUsage)) + "/" + Switch.b2Any(mInfo.getPlan_ram()));
    }

    private void getHostInfo(JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        ApiGate.get("getLiveServiceInfo", params, handler);
    }

    @Override
    public void onRefresh() {
        getHostInfo(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                HostInfo info = gson.fromJson(response.toString(), HostInfo.class);
                Logger.d(response);
                mSwipeRefreshLayout.setRefreshing(false);
                if (info.getError() == 0) {
                    mInfo = info;
                    bindView();
                } else {
                    error(info.getError());
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        saveCache();
        super.onDestroy();
    }

    private void saveCache(){
        if(mInfo==null){
            return;
        }
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mHost.getID(),new Gson().toJson(mInfo));
        editor.apply();
    }

    @OnClick(R.id.header)
    public void onHeaderClick(){
        this.supportFinishAfterTransition();
    }

    @OnClick(R.id.node_location)
    public void onHostLocationClick(){
    }

    @OnClick(R.id.node_os)
    public void HostOSClick(){

    }

    @OnClick(R.id.node_ip)
    public void HostIPClick(){

    }


    @OnClick(R.id.node_ssh_port)
    public void HostSSHPortClick(){

    }


    @OnClick(R.id.node_status)
    public void HostStatusClick(){

    }


    @OnClick(R.id.node_cpu)
    public void HostCPUClick(){

    }

    @OnClick(R.id.bandwidth_usage)
    public void HostBandwidthUsageClick(){

    }


    @OnClick(R.id.ram_usage)
    public void HostRAMUsageClick(){

    }

}
