package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.File;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.net.ApiGate;

import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class TextFileActivity extends AppCompatActivity {
    @BindView(R.id.file_content_view)
    EditText mFileContentView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private Host mHost;
    private File mFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHost = (Host) intent.getSerializableExtra("host");
        if (mHost == null) {
            this.finish();
            return;
        }
        mFile = (File) intent.getSerializableExtra("file");

        setContentView(R.layout.activity_text_file);
        ButterKnife.bind(this);

        mFileContentView.setTextIsSelectable(true);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refresh();
            }
        });

        refresh();
    }

    private void refresh() {
        if (mFile == null) {
            return;
        }
        String command = "cat " + mFile.getAbsolutePath();
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
                    mFileContentView.setText(msg.getMessage());
                    setTitle(mFile.getFileName());
                } else {
                    mFileContentView.setText("错误：");
                    mFileContentView.append(msg.getError() + "");
                    mFileContentView.append(msg.getMessage());
                }
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }
}
