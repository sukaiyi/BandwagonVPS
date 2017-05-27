package com.sukaiyi.bandwagonvps;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.meetic.marypopup.MaryPopup;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.HostInfo;
import com.sukaiyi.bandwagonvps.bean.Password;
import com.sukaiyi.bandwagonvps.net.ApiGate;
import com.sukaiyi.bandwagonvps.utils.Switch;
import com.sukaiyi.bandwagonvps.utils.Utils;
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

    private MaryPopup mMaryPopup;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    @BindView(R.id.header)
    CardView mHeader;
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
    @BindView(R.id.node_root_password)
    HostSimpleItemView mHostRootPassword;
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
        if(mInfo.getError()!=0){
            return;
        }
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

    @OnClick({
            R.id.node_location,
            R.id.node_ip,
            R.id.node_status,
            R.id.node_ssh_port,
            R.id.node_cpu
    })
    public void onHostNormalItemClick(View v) {
        if(mMaryPopup==null){
            mMaryPopup = MaryPopup.with(this)
                    .cancellable(true)
                    .blackOverlayColor(Color.parseColor("#DD444444"))
                    .backgroundColor(Color.parseColor("#EFF4F5"))
                    .center(true);
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_host_item_detail,null,false);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        final TextView valueView = (TextView) view.findViewById(R.id.value);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.copyToClipboard(HostDetailActivity.this,valueView.getText().toString());
                Snackbar.make(v,valueView.getText().toString()+" 已复制到剪切板",Snackbar.LENGTH_SHORT).show();
            }
        });
        HostSimpleItemView clickedView = (HostSimpleItemView) v;
        titleView.setText(clickedView.getTitle());
        valueView.setText(clickedView.getValue());
        mMaryPopup.content(view)
                .from(v)
                .show();
    }

    @OnClick(R.id.node_os)
    public void onHostOSClick() {
        if(mMaryPopup==null){
            mMaryPopup = MaryPopup.with(this)
                    .cancellable(true)
                    .blackOverlayColor(Color.parseColor("#DD444444"))
                    .backgroundColor(Color.parseColor("#EFF4F5"))
                    .center(true);
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_host_item_detail_clickable,null,false);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView tipsView = (TextView) view.findViewById(R.id.tips);
        final TextView valueView = (TextView) view.findViewById(R.id.value);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.copyToClipboard(HostDetailActivity.this,valueView.getText().toString());
                Snackbar.make(v,valueView.getText().toString()+" 已复制到剪切板",Snackbar.LENGTH_SHORT).show();
            }
        });
        TextView buttonView = (TextView) view.findViewById(R.id.button);
        titleView.setText(mHostOS.getTitle());
        valueView.setText(mHostOS.getValue());
        tipsView.setText("重新安装系统会清除所有数据");
        buttonView.setText("更换");
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaryPopup.close(true);
                Intent intent = new Intent(HostDetailActivity.this, ChangeOSActivity.class);
                intent.putExtra("host", mHost);

                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(
                        HostDetailActivity.this,
                        valueView,
                        "transition_os"
                );
                startActivity(intent, options.toBundle());
            }
        });

        mMaryPopup.content(view)
                .from(mHostOS)
                .show();
    }

    @OnClick(R.id.node_root_password)
    public void onRootPasswordClick() {
        if(mMaryPopup==null){
            mMaryPopup = MaryPopup.with(this)
                    .cancellable(true)
                    .blackOverlayColor(Color.parseColor("#DD444444"))
                    .backgroundColor(Color.parseColor("#EFF4F5"))
                    .center(true);
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_host_item_detail_clickable,null,false);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        TextView tipsView = (TextView) view.findViewById(R.id.tips);
        final TextView valueView = (TextView) view.findViewById(R.id.value);
        TextView buttonView = (TextView) view.findViewById(R.id.button);
        valueView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.copyToClipboard(HostDetailActivity.this,valueView.getText().toString());
                Snackbar.make(v,valueView.getText().toString()+" 已复制到剪切板",Snackbar.LENGTH_SHORT).show();
            }
        });
        titleView.setText(mHostRootPassword.getTitle());
        tipsView.setText("无法获取root密码，只能直接生成");
        valueView.setText(mHostRootPassword.getValue());
        buttonView.setText("生成");
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                apiCall("resetRootPassword",new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Gson gson = new Gson();
                        Password psd = gson.fromJson(response.toString(), Password.class);
                        if(psd.getError()==0){
                            valueView.setText(psd.getPassword());
                        }else{
                            ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                            snack(msg);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                        super.onFailure(statusCode, headers, responseString, throwable);
                        snack(new ErrorMessage(statusCode,responseString));
                    }

                });
            }
        });

        mMaryPopup.content(view)
                .from(mHostRootPassword)
                .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSwipeRefreshLayout.setRefreshing(false);
        ApiGate.cancelAllRequests();
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
        } else if(mMaryPopup!=null && mMaryPopup.isOpened()){
            mMaryPopup.close(true);
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
