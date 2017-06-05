package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;

import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.adapter.SnapshotAdapter;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.Snapshot;
import com.sukaiyi.bandwagonvps.bean.Snapshots;
import com.sukaiyi.bandwagonvps.net.ApiGate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

import static com.sukaiyi.bandwagonvps.R.id.toolbar;

public class SnapshotActivity extends AppCompatActivity {

    @BindView(toolbar)
    Toolbar mToolbar;
    @BindView(R.id.snapshot_list)
    RecyclerView mSnapshotList;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.action_button_create_snapshot)
    FloatingActionButton mActionButtonCreateSnapshot;
    @BindView(R.id.action_button_import_snapshot)
    FloatingActionButton mActionButtonImportSnapshot;
    @BindView(R.id.menu_vertical)
    FloatingActionMenu mMenuVertical;

    private Host mHost;
    private SnapshotAdapter mSnapshotAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHost = (Host) intent.getSerializableExtra("host");
        if (mHost == null) {
            this.finish();
            return;
        }
        setContentView(R.layout.activity_snapshot_activiry);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        mToolbar.setLogo(R.drawable.ic_vpn_white_24dp);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshSnapshotList();
            }
        });
        mSnapshotList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mSnapshotAdapter = new SnapshotAdapter(this, new ArrayList<Snapshot>(), R.layout.snapshot_list_header);
        mSnapshotAdapter.bindToRecyclerView(mSnapshotList);
        mSnapshotAdapter.setEmptyView(R.layout.list_empty);

        refreshSnapshotList();
    }

    private void apiCall(String api, JsonHttpResponseHandler handler) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        ApiGate.get(api, params, handler);
    }

    private void refreshSnapshotList() {
        apiCall("snapshot/list", new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                Snapshots snapshots = gson.fromJson(response.toString(), Snapshots.class);
                if(snapshots.getError()==0){
                    mSnapshotAdapter.getData().clear();
                    mSnapshotAdapter.addData(Arrays.asList(snapshots.getSnapshots()));
                }

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (mMenuVertical.isOpened()) {
            Rect rect = new Rect();
            mMenuVertical.getGlobalVisibleRect(rect);
            float x = ev.getRawX();
            float y = ev.getRawY();
            boolean contains = rect.contains((int) x, (int) y);
            if (contains) {
                return super.dispatchTouchEvent(ev);
            } else {
                mMenuVertical.close(true);
                return true;
            }
        } else {
            return super.dispatchTouchEvent(ev);
        }
    }

    @OnClick(R.id.action_button_import_snapshot)
    public void onImportSnapshotButtonClick() {

    }

    @OnClick(R.id.action_button_create_snapshot)
    public void onCreateSnapshotButtonClick() {

    }
}
