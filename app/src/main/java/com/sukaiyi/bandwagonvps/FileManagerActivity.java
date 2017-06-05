package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.adapter.FileListAdapter;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.File;
import com.sukaiyi.bandwagonvps.bean.FileParser;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.net.ApiGate;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class FileManagerActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {

    @BindView(R.id.file_list)
    RecyclerView mFileList;
    @BindView(R.id.current_path)
    TextView mCurrentPath;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private FileListAdapter mFileListAdapter;
    private Stack<String> mPath;
    private Host mHost;

    private Map<String, List<File>> mFileListCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHost = (Host) intent.getSerializableExtra("host");
        if (mHost == null) {
            this.finish();
            return;
        }
        setContentView(R.layout.activity_file_manager);
        ButterKnife.bind(this);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setElevation(0);
        }
        setTitle(mHost.getName());

        mFileList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mFileListAdapter = new FileListAdapter(this, new ArrayList<File>());
        mFileListAdapter.bindToRecyclerView(mFileList);
        mFileListAdapter.setOnItemClickListener(this);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshFileList();
            }
        });


        mPath = new Stack<>();
        mPath.push("root");
        mFileListCache = new HashMap<>();

        if(!getFileListFromCache()){
            refreshFileList();
        }
    }

    private void snack(ErrorMessage msg) {
        if (msg.getError() != 0) {
            Snackbar.make(mFileList,
                    "错误:" + msg.getError() + "," + msg.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mFileList,
                    "操作成功",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void refreshFileList() {
        final String path = getPath();
        String command = "ls -all " + path + " | sort -k9";
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("command", command.trim());
        ApiGate.get("basicShell/exec", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response.toString());
                ErrorMessage msg = new Gson().fromJson(response.toString(), ErrorMessage.class);
                if (msg.getError() == 0) {
                    File[] files = new FileParser().parse(path, msg.getMessage());
                    mFileListAdapter.getData().clear();
                    mFileListAdapter.addData(Arrays.asList(files));
                    mFileListAdapter.sort();
                    mFileListCache.put(path, Arrays.asList(files));
                    mCurrentPath.setText(path);
                } else {
                    snack(msg);
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private boolean getFileListFromCache() {
        if (mFileListCache == null) {
            mFileListCache = new HashMap<>();
            return false;
        }
        String path = getPath();
        if (mFileListCache.containsKey(path)) {
            mFileListAdapter.getData().clear();
            mFileListAdapter.addData(mFileListCache.get(path));
            mFileListAdapter.sort();
            mCurrentPath.setText(path);
            return true;
        }
        return false;
    }

    @NonNull
    private String getPath() {
        StringBuilder stringBuffer = new StringBuilder();
        stringBuffer.append("/");

        Iterator<String> iterator = mPath.iterator();
        while (iterator.hasNext()) {
            stringBuffer.append(iterator.next());
            stringBuffer.append("/");
        }
        if (stringBuffer.length() > 1) {
            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
        }
        return stringBuffer.toString();
    }

    @Override
    public void onBackPressed() {
        if (mPath.empty()) {
            super.onBackPressed();
        } else {
            mPath.pop();
            if(!getFileListFromCache()){
                refreshFileList();
            }
        }
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        File file = mFileListAdapter.getItem(position);
        if (file == null) return;

        if (file.isDirectory()) {
            onDirectoryClicked(file);
        } else if (file.isLink()) {
            onLinkClicked(file);
        } else {
            onFileClicked(file);
        }
    }

    private void onFileClicked(File file) {
        Intent intent = new Intent(FileManagerActivity.this, TextFileActivity.class);
        intent.putExtra("host", mHost);
        intent.putExtra("file", file);
        startActivity(intent);
    }

    private void onLinkClicked(File file) {
        if (!file.isLink()) {
            return;
        }
        mPath.clear();
        String[] data = file.getLinkPath().trim().split("/");
        for (String d : data) {
            if (TextUtils.isEmpty(d)) {
                continue;
            }
            mPath.push(d);
        }
        if (!getFileListFromCache()) {
            refreshFileList();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        ApiGate.cancelAllRequests();
    }

    private void onDirectoryClicked(File file) {
        if (!file.isDirectory()) {
            return;
        }
        if (file.getFileName().equals("..")) {
            if (!mPath.empty()) {
                mPath.pop();
                if (!getFileListFromCache()) {
                    refreshFileList();
                }
            }
        } else if (file.getFileName().equals(".")) {
        } else {
            mPath.push(file.getFileName());
            if (!getFileListFromCache()) {
                refreshFileList();
            }
        }
    }
}
