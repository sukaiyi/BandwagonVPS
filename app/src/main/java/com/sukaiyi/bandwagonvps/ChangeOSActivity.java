package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.meetic.marypopup.MaryPopup;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.adapter.OSAdapter;
import com.sukaiyi.bandwagonvps.bean.AvailableOS;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.HostInfo;
import com.sukaiyi.bandwagonvps.net.ApiGate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ChangeOSActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.current_os)
    TextView mCurrentOsView;
    @BindView(R.id.os_list)
    RecyclerView mOsListView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private HostInfo mInfo;
    private Host mHost;
    private AvailableOS mAvailableOS;
    private OSAdapter mOSAdapter;
    private MaryPopup mMaryPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHost = (Host) intent.getSerializableExtra("host");
        if (mHost == null) {
            this.finish();
            return;
        }
        setContentView(R.layout.activity_change_os);
        ButterKnife.bind(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_vpn_white_24dp);

        loadCache();
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mOsListView.setLayoutManager(new LinearLayoutManager(this, LinearLayout.VERTICAL, false));

        mOSAdapter = new OSAdapter(this, new ArrayList<String>(), R.layout.os_list_header);
        mOSAdapter.bindToRecyclerView(mOsListView);
        mOSAdapter.setEmptyView(R.layout.list_empty);
        mOSAdapter.setOnItemClickListener(this);

        onRefresh();
    }

    private void loadCache() {
        SharedPreferences sharedPreferences = getSharedPreferences("HostDetailActivity", MODE_PRIVATE);
        String info = sharedPreferences.getString(mHost.getID(), "");
        if (TextUtils.isEmpty(info)) {
            return;
        }
        mInfo = new Gson().fromJson(info, HostInfo.class);
        bindView();
    }

    private void bindView() {
        if (mInfo != null) {
            mCurrentOsView.setText(mInfo.getOs());
        }

        if (mAvailableOS != null && mAvailableOS.getError() == 0) {
            mCurrentOsView.setText(mAvailableOS.getInstalled());
            mOSAdapter.getData().clear();
            mOSAdapter.addData(Arrays.asList(mAvailableOS.getTemplates()));
        }
    }

    private void apiCall(String api, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        ApiGate.get(api, params, handler);
    }

    @Override
    public void onBackPressed() {
        if (mMaryPopup != null && mMaryPopup.isOpened()) {
            mMaryPopup.close(true);
        } else {
            this.finish();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApiGate.cancelAllRequests();
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onRefresh() {
        apiCall("getAvailableOS", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                mSwipeRefreshLayout.setRefreshing(false);
                Gson gson = new Gson();
                mAvailableOS = gson.fromJson(response.toString(), AvailableOS.class);
                if (mAvailableOS.getError() == 0) {
                    bindView();
                } else {
                    ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                    snack(msg);
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
                mSwipeRefreshLayout.setRefreshing(false);
                snack(new ErrorMessage(statusCode, responseString));
            }
        });
    }

    private void snack(ErrorMessage msg) {
        if (msg.getError() != 0) {
            Snackbar.make(mOsListView,
                    "错误:" + msg.getError() + "," + msg.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mOsListView,
                    "操作成功",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onItemClick(final BaseQuickAdapter adapter, View v, final int position) {
        if (mMaryPopup == null) {
            mMaryPopup = MaryPopup.with(this)
                    .cancellable(true)
                    .blackOverlayColor(Color.parseColor("#DD444444"))
                    .backgroundColor(Color.parseColor("#EFF4F5"))
                    .center(true);
        }
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_confirm_change_os, null, false);
        TextView currentOSView = (TextView) view.findViewById(R.id.current_os);
        TextView toOSView = (TextView) view.findViewById(R.id.to_os);
        TextView buttonView = (TextView) view.findViewById(R.id.button);
        currentOSView.setText(mCurrentOsView.getText());
        final String osToInstall = adapter.getData().get(position).toString();
        toOSView.setText(osToInstall);
        buttonView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMaryPopup.close(true);
                RequestParams params = new RequestParams();
                params.put("veid", mHost.getID());
                params.put("api_key", mHost.getApiKey());
                params.put("os", osToInstall);
                ApiGate.get("reinstallOS", params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        super.onSuccess(statusCode, headers, response);
                        Gson gson = new Gson();
                        ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                        snack(msg);
                        onRefresh();
                    }
                });
            }
        });

        mMaryPopup.content(view)
                .from(v)
                .show();
    }
}
