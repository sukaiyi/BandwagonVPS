package com.sukaiyi.bandwagonvps;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.gson.Gson;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.adapter.SnapshotAdapter;
import com.sukaiyi.bandwagonvps.bean.ErrorMessage;
import com.sukaiyi.bandwagonvps.bean.ExportSnapshotBean;
import com.sukaiyi.bandwagonvps.bean.Host;
import com.sukaiyi.bandwagonvps.bean.Snapshot;
import com.sukaiyi.bandwagonvps.bean.Snapshots;
import com.sukaiyi.bandwagonvps.net.ApiGate;
import com.sukaiyi.bandwagonvps.utils.Utils;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cz.msebera.android.httpclient.Header;

import static com.sukaiyi.bandwagonvps.R.id.toolbar;

public class SnapshotActivity extends AppCompatActivity implements BaseQuickAdapter.OnItemClickListener {

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
        setTitle(mHost.getName());
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
        mSnapshotAdapter.setEmptyView(R.layout.loading_layout);
        mSnapshotAdapter.setOnItemClickListener(this);

        refreshSnapshotList();
    }

    private void snack(ErrorMessage msg) {
        if (msg.getError() != 0) {
            Snackbar.make(mSnapshotList,
                    "错误:" + msg.getError() + "," + msg.getMessage(),
                    Snackbar.LENGTH_LONG).show();
        } else {
            Snackbar.make(mSnapshotList,
                    "操作成功",
                    Snackbar.LENGTH_LONG).show();
        }
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
                if (snapshots.getError() == 0) {
                    if (snapshots.getSnapshots().length == 0) {
                        mSnapshotAdapter.setEmptyView(R.layout.list_empty);
                    } else {
                        mSnapshotAdapter.getData().clear();
                        mSnapshotAdapter.addData(Arrays.asList(snapshots.getSnapshots()));
                    }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_snapshot_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_snapshot_help:
                help();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void help() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_snapshot_help))
                .setMessage(getString(R.string.str_snapshot_help_message))
                .setPositiveButton(getString(R.string.str_dialog_ok), null)
                .show();
    }

    private void export(Snapshot snapshot) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("snapshot", snapshot.getFileName());
        ApiGate.get("snapshot/export", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                ExportSnapshotBean exportSnapshotBean = gson.fromJson(response.toString(), ExportSnapshotBean.class);
                if (exportSnapshotBean.getError() == 0) {
                    showExportResult(exportSnapshotBean);
                } else {
                    ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                    snack(msg);
                }
            }
        });
    }

    private void showExportResult(final ExportSnapshotBean exportSnapshotBean) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_token))
                .setMessage(exportSnapshotBean.getToken())
                .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Utils.copyToClipboard(SnapshotActivity.this, exportSnapshotBean.getToken());
                        Snackbar.make(mSnapshotList, getString(R.string.str_token_copied), Snackbar.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.str_dialog_cancel), null)
                .show();
    }

    private void confirmDelete(final Snapshot snapshot) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_delete))
                .setMessage(getString(R.string.str_delete_message))
                .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        delete(snapshot);
                    }
                })
                .show();
    }

    private void delete(final Snapshot snapshot) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("snapshot", snapshot.getFileName());
        ApiGate.get("snapshot/delete", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                snack(msg);
                refreshSnapshotList();
            }
        });
    }

    private void confirmRestore(final Snapshot snapshot) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_restore))
                .setMessage(getString(R.string.str_restore_message))
                .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        restore(snapshot);
                    }
                })
                .show();
    }

    private void restore(Snapshot snapshot) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("snapshot", snapshot.getFileName());
        ApiGate.get("snapshot/restore", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                snack(msg);
            }
        });

    }

    private void download(Snapshot snapshot) {
        DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(snapshot.getDownloadLink());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        downloadManager.enqueue(request);
    }

    private void confirmSticky(final Snapshot snapshot) {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.str_sticky))
                .setMessage(getString(R.string.str_sticky_message))
                .setPositiveButton(getString(R.string.str_dialog_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sticky(snapshot);
                    }
                })
                .show();
    }

    private void sticky(Snapshot snapshot) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("snapshot", snapshot.getFileName());
        params.put("sticky", snapshot.getSticky() ? 0 : 1);
        ApiGate.get("snapshot/toggleSticky", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                snack(msg);
                refreshSnapshotList();
            }
        });
    }

    @Override
    public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
        final Snapshot snapshot = mSnapshotAdapter.getItem(position);
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.snapshot_list_item_popup_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_popup_download:
                        download(snapshot);
                        break;
                    case R.id.menu_popup_restore:
                        confirmRestore(snapshot);
                        break;
                    case R.id.menu_popup_delete:
                        confirmDelete(snapshot);
                        break;
                    case R.id.menu_popup_export:
                        export(snapshot);
                        break;
                    case R.id.menu_popup_sticky:
                        confirmSticky(snapshot);
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popupMenu.show(); // 显示弹出菜单
    }

    @OnClick(R.id.action_button_import_snapshot)
    public void onImportSnapshotButtonClick() {
        confirmImportSnapshot();
    }

    @Override
    public void onBackPressed() {
        if (mMenuVertical.isOpened()) {
            mMenuVertical.close(true);
        } else {
            super.onBackPressed();
        }
    }

    private void confirmImportSnapshot() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.str_import_snapshot))
                .customView(R.layout.dialog_import_snapshot, true)
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
                        EditText tokenText = (EditText) view.findViewById(R.id.token);
                        String id = idText.getText().toString();
                        String token = tokenText.getText().toString();
                        boolean pass = true;
                        if (TextUtils.isEmpty(id)) {
                            pass = false;
                        }
                        if (TextUtils.isEmpty(token)) {
                            pass = false;
                        }
                        if (pass) {
                            importSnapshot(id, token);
                        } else {
                            Snackbar.make(null, "没有输入信息", Snackbar.LENGTH_LONG).show();
                        }
                    }
                })
                .show();

    }

    private void importSnapshot(String id, String token) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        params.put("sourceVeid", id);
        params.put("sourceToken", token);
        ApiGate.get("snapshot/import", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                snack(msg);
            }
        });
    }

    @OnClick(R.id.action_button_create_snapshot)
    public void onCreateSnapshotButtonClick() {
        confirmCreateSnapshot();
    }

    private void confirmCreateSnapshot() {
        new MaterialDialog.Builder(this)
                .title(getString(R.string.str_create_snapshot))
                .customView(R.layout.dialog_create_snapshot, true)
                .positiveText(getString(R.string.str_dialog_ok))
                .negativeText(getString(R.string.str_dialog_cancel))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        View view = dialog.getCustomView();
                        if (view == null) {
                            return;
                        }
                        EditText idText = (EditText) view.findViewById(R.id.description);
                        String description = idText.getText().toString();
                        createSnapshot(description);
                    }
                })
                .show();
    }

    private void createSnapshot(String description) {
        RequestParams params = new RequestParams();
        params.put("veid", mHost.getID());
        params.put("api_key", mHost.getApiKey());
        if (!TextUtils.isEmpty(description)) {
            params.put("description", description);
        }
        ApiGate.get("snapshot/create", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
                Gson gson = new Gson();
                ErrorMessage msg = gson.fromJson(response.toString(), ErrorMessage.class);
                if (msg.getError() == 0) {
                    Snackbar.make(mSnapshotList, getString(R.string.str_create_snapshot_success_tips), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }
}
