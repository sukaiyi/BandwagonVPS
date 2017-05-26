package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
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

    @BindView(R.id.header)
    ConstraintLayout mHeader;
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

    @BindView(R.id.action_button_start)
    FloatingActionButton mActionButtonStart;
    @BindView(R.id.action_button_stop)
    FloatingActionButton mActionButtonStop;
    @BindView(R.id.action_button_restart)
    FloatingActionButton mActionButtonRestart;
    @BindView(R.id.action_button_kill)
    FloatingActionButton mActionButtonKill;
    @BindView(R.id.menu_vertical)
    FloatingActionMenu mFloatingActionVerticalMenu;

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_vpn_white_24dp);
        ButterKnife.bind(this);

        loadCache();
        onRefresh();
        mSwipeRefreshLayout.setOnRefreshListener(this);

    }

    private void loadCache() {
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        String info = sharedPreferences.getString(mHost.getID(), "");
        if (TextUtils.isEmpty(info)) {
            return;
        }
        mInfo = new Gson().fromJson(info, HostInfo.class);
        bindView();
    }

    private void snack(ErrorMessage msg) {
        if (msg.getError() != 0) {
            Snackbar.make(mFloatingActionVerticalMenu,
                    "错误:" + msg.getError() + "," + msg.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mFloatingActionVerticalMenu,
                    "操作成功",
                    Snackbar.LENGTH_LONG).show();
        }
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
        if (ramUsage.equals("-")) {
            ramUsage = "0";
        }
        mRamUsage.setValue(Switch.b2Any(Integer.parseInt(ramUsage)) + "/" + Switch.b2Any(mInfo.getPlan_ram()));
    }

    private void apiCall(String api, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        ApiGate.get(api, params, handler);
    }

    @Override
    public void onRefresh() {
        apiCall("getLiveServiceInfo", new JsonHttpResponseHandler() {
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
                    ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                    snack(msg);
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        saveCache();
        super.onDestroy();
    }

    private void saveCache() {
        if (mInfo == null) {
            return;
        }
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(mHost.getID(), new Gson().toJson(mInfo));
        editor.apply();
    }

    @OnClick(R.id.header)
    public void onHeaderClick() {

    }

    @OnClick(R.id.node_location)
    public void onHostLocationClick() {
    }

    @OnClick(R.id.node_os)
    public void onHostOSClick() {
    }

    @OnClick(R.id.node_ip)
    public void onHostIPClick() {
    }

    @OnClick(R.id.node_ssh_port)
    public void onHostSSHPortClick() {
    }

    @OnClick(R.id.node_status)
    public void onHostStatusClick() {
    }

    @OnClick(R.id.node_cpu)
    public void onHostCPUClick() {
    }

    @OnClick(R.id.bandwidth_usage)
    public void onHostBandwidthUsageClick() {
    }

    @OnClick(R.id.ram_usage)
    public void onHostRAMUsageClick() {
    }

    @Override
    public void onBackPressed() {
        if (mFloatingActionVerticalMenu.isOpened()) {
            mFloatingActionVerticalMenu.close(true);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mFloatingActionVerticalMenu.isOpened()) {
            Rect rect = new Rect();
            mFloatingActionVerticalMenu.getGlobalVisibleRect(rect);
            float x = ev.getRawX();
            float y = ev.getRawY();
            boolean contains = rect.contains((int) x, (int) y);
            if (contains) {
                return super.dispatchTouchEvent(ev);
            } else {
                mFloatingActionVerticalMenu.close(true);
                return true;
            }
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @OnClick({
            R.id.action_button_start,
            R.id.action_button_stop,
            R.id.action_button_restart,
            R.id.action_button_kill
    })
    public void onClick(View view) {
        mFloatingActionVerticalMenu.close(true);

        JsonHttpResponseHandler handler = new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Gson gson = new Gson();
                ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                snack(msg);
                if (msg.getError() == 0) {
                    onRefresh();
                }
            }
        };
        switch (view.getId()) {
            case R.id.action_button_start:
                apiCall("start", handler);
                break;
            case R.id.action_button_stop:
                apiCall("stop", handler);
                break;
            case R.id.action_button_restart:
                apiCall("restart", handler);
                break;
            case R.id.action_button_kill:
                apiCall("kill", handler);
                break;
        }
    }
}
