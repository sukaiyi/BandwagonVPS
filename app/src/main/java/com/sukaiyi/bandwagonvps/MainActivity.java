package com.sukaiyi.bandwagonvps;

import android.Manifest;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.sukaiyi.bandwagonvps.adapter.HostListAdapter;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.HostInfo;
import com.sukaiyi.bandwagonvps.net.ApiGate;
import com.sukaiyi.bandwagonvps.utils.CrashHandler;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        BaseQuickAdapter.OnItemLongClickListener {

    @BindView(R.id.host_list)
    RecyclerView mHostListView;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private HostListAdapter mHostListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setLogo(R.drawable.ic_vpn_white_24dp);
        ButterKnife.bind(this);

        mHostListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        mHostListAdapter = new HostListAdapter(new ArrayList<Host>());
        mHostListAdapter.bindToRecyclerView(mHostListView);
        mHostListAdapter.setEmptyView(R.layout.list_empty);

        mHostListAdapter.setOnItemClickListener(this);
        mHostListAdapter.setOnItemLongClickListener(this);

        loadData();
        mSwipeRefreshLayout.setOnRefreshListener(this);

        requestStoragePermissions();
    }

    private void requestStoragePermissions() {
        String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, getString(R.string.str_permission_request_tips), 100, perms);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void loadData() {
        mHostListAdapter.getData().clear();
        Gson gson = new Gson();
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        Map<String, ?> map = sp.getAll();
        Set<String> keys = map.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String key = iterator.next();
            String value = map.get(key).toString();
            Host host = gson.fromJson(value, Host.class);
            mHostListAdapter.addData(host);
        }
    }

    private void saveData(Host host) {
        Gson gson = new Gson();

        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(host.getID(), gson.toJson(host));
        editor.apply();
    }

    private void deleteData(Host host) {
        SharedPreferences sp = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(host.getID());
        editor.apply();
    }

    public void addNewHost() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.str_add_host))
                .customView(R.layout.dialog_add_host, true)
                .positiveText(getString(R.string.str_dialog_ok))
                .negativeText(getString(R.string.str_dialog_cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        if (view == null) {
                            return;
                        }
                        EditText idText = (EditText) view.findViewById(R.id.host_id);
                        EditText apiKeyText = (EditText) view.findViewById(R.id.host_api_key);
                        String id = idText.getText().toString();
                        String apiKey = apiKeyText.getText().toString();
                        boolean pass = true;
                        if (TextUtils.isEmpty(id)) {
                            pass = false;
                        }
                        if (TextUtils.isEmpty(apiKey)) {
                            pass = false;
                        }
                        if (pass) {
                            Host host = new Host(id, id, apiKey);
                            saveData(host);
                            mHostListAdapter.addData(host);
                            refreshName(host);
                        } else {
                            Snackbar.make(null, "没有输入信息", Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .show();
    }

    private void refreshName(final Host host) {
        RequestParams params = new RequestParams();
        params.put("veid", host.getID());
        params.put("api_key", host.getApiKey());
        ApiGate.get("getServiceInfo", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Gson gson = new Gson();
                HostInfo info = gson.fromJson(response.toString(), HostInfo.class);
                mSwipeRefreshLayout.setRefreshing(false);
                if (info.getError() == 0) {
                    host.setName(info.getHostname());
                    host.setEmail(info.getEmail());
                    host.setPlan(info.getPlan());
                    saveData(host);
                    mHostListAdapter.notifyDataSetChanged();
                } else {
                    ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                    snack(msg);
                }
            }
        });
    }

    private void snack(ErrorMessage msg) {
        if (msg.getError() != 0) {
            Snackbar.make(mHostListView,
                    "错误:" + msg.getError() + "," + msg.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mHostListView,
                    "操作成功",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_add_host:
                addNewHost();
                break;
            case R.id.menu_import_host:
                importHost();
                break;
            case R.id.menu_export_host:
                exportHost();
                break;
            case R.id.menu_send_log:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.str_send_log))
                        .setMessage(getString(R.string.str_send_log_message))
                        .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                CrashHandler.getInstance().sendCrashLog(MainActivity.this);
                            }
                        })
                        .show();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exportHost() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_export))
                .setMessage(getString(R.string.str_export_message))
                .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = false;
                        requestStoragePermissions();
                        File file = Environment.getExternalStorageDirectory();
                        String path = file.getAbsolutePath() + "/bandwagon_host_backup.json";
                        BufferedWriter writer;
                        try {
                            writer = new BufferedWriter(new FileWriter(path));
                            writer.write(new Gson().toJson(mHostListAdapter.getData()));
                            writer.close();
                            success = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (success) {
                            Snackbar.make(mHostListView, "导出成功：" + path, Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(mHostListView, "导出失败", Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.str_dialog_cancel), null)
                .show();
    }

    private void importHost() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_import_host))
                .setMessage(getString(R.string.str_import_message))
                .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        boolean success = false;
                        requestStoragePermissions();
                        File file = Environment.getExternalStorageDirectory();
                        BufferedReader reader;
                        StringBuilder sb = new StringBuilder();
                        try {
                            reader = new BufferedReader(new FileReader(file.getAbsolutePath() + "/bandwagon_host_backup.json"));
                            String line;
                            while ((line = reader.readLine()) != null) {
                                sb.append(line).append("\n");
                            }
                            reader.close();
                            List<Host> hosts = new Gson().fromJson(sb.toString(), new TypeToken<List<Host>>() {
                            }.getType());
                            for (Host h : hosts) {
                                saveData(h);
                            }
                            loadData();
                            success = true;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        if (success) {
                            Snackbar.make(mHostListView, "导入成功", Snackbar.LENGTH_LONG).show();
                        } else {
                            Snackbar.make(mHostListView, "导入失败", Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .setNegativeButton(getString(R.string.str_dialog_cancel), null)
                .show();
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        Intent intent = new Intent(MainActivity.this, HostDetailActivity.class);
        intent.putExtra("host", mHostListAdapter.getData().get(position));

        ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                Pair.create(view, "transition_card_view"),
                Pair.create(view.findViewById(R.id.host_name_view), "transition_host_name"),
                Pair.create(view.findViewById(R.id.host_id_view), "transition_host_id"),
                Pair.create(view.findViewById(R.id.host_plan_view), "transition_plan_view"),
                Pair.create(view.findViewById(R.id.host_email_view), "transition_email_view")
        );
        startActivity(intent, options.toBundle());
    }

    private void modifyData(final int position, final Host host) {
        MaterialDialog dialog = new MaterialDialog.Builder(this)
                .title("修改")
                .customView(R.layout.dialog_add_host, true)
                .positiveText("确定")
                .negativeText("取消")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        if (view == null) {
                            return;
                        }
                        EditText idText = (EditText) view.findViewById(R.id.host_id);
                        EditText apiKeyText = (EditText) view.findViewById(R.id.host_api_key);
                        String id = idText.getText().toString();
                        String apiKey = apiKeyText.getText().toString();
                        boolean pass = true;
                        if (TextUtils.isEmpty(id)) {
                            pass = false;
                        }
                        if (TextUtils.isEmpty(apiKey)) {
                            pass = false;
                        }
                        if (pass) {
                            Host h = new Host(host.getName(), id, apiKey);
                            saveData(h);
                            mHostListAdapter.remove(position);
                            mHostListAdapter.addData(position, h);
                        }
                    }
                }).build();
        View view = dialog.getCustomView();
        EditText idText = (EditText) view.findViewById(R.id.host_id);
        EditText apiKeyText = (EditText) view.findViewById(R.id.host_api_key);
        idText.setText(host.getID());
        apiKeyText.setText(host.getApiKey());
        dialog.show();
    }

    @Override
    public void onRefresh() {
        loadData();
        for (Host host : mHostListAdapter.getData()) {
            refreshName(host);
        }
    }

    @Override
    public boolean onItemLongClick(BaseQuickAdapter adapter, View view, final int position) {
        PopupMenu popupMenu = new PopupMenu(MainActivity.this, view);
        popupMenu.getMenuInflater().inflate(R.menu.host_list_item_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_popup_delete:
                        new MaterialDialog.Builder(MainActivity.this)
                                .title("确认删除吗")
                                .content(mHostListAdapter.getData().get(position).getName())
                                .positiveText("删除")
                                .negativeText("取消")
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        deleteData(mHostListAdapter.getData().get(position));
                                        mHostListAdapter.remove(position);
                                    }
                                }).show();
                        break;
                    case R.id.menu_popup_modify:
                        modifyData(position, mHostListAdapter.getData().get(position));
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popupMenu.show(); // 显示弹出菜单
        return true;
    }
}
