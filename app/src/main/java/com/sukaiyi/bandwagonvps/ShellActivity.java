package com.sukaiyi.bandwagonvps;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;

import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.adapter.ShellListAdapter;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.Shell;
import com.sukaiyi.bandwagonvps.net.ApiGate;

import org.json.JSONObject;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;

public class ShellActivity extends AppCompatActivity implements TextWatcher {

    @BindView(R.id.shell_command_list)
    RecyclerView mShellCommandList;

    private ShellListAdapter mShellListAdapter;
    private Host mHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        mHost = (Host) intent.getSerializableExtra("host");
        if (mHost == null) {
            this.finish();
            return;
        }
        setContentView(R.layout.activity_shell);
        ButterKnife.bind(this);

        mShellListAdapter = new ShellListAdapter(this, new ArrayList<Shell>());
        mShellCommandList.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mShellListAdapter.bindToRecyclerView(mShellCommandList);
        mShellListAdapter.addData(new Shell(Shell.TYPE_REQUEST, "", "/", false));
    }

    private String getCommand(String str) {
        int index = str.lastIndexOf('#');
        return str.substring(index + 1, str.length() - 1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ApiGate.cancelAllRequests();
    }

    private void exec(String command) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("command", command);
        ApiGate.get("basicShell/exec", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response.toString());
                ErrorMessage msg = new Gson().fromJson(response.toString(), ErrorMessage.class);
            }
        });
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
