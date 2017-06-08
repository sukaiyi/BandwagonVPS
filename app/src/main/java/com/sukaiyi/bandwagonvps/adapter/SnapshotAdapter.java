package com.sukaiyi.bandwagonvps.adapter;

import android.content.Context;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sukaiyi.bandwagonvps.R;
import com.sukaiyi.bandwagonvps.bean.Snapshot;
import com.sukaiyi.bandwagonvps.utils.Switch;

import java.util.List;

import static com.sukaiyi.bandwagonvps.R.id.snapshot_purges_in;

public class SnapshotAdapter extends BaseQuickAdapter<Snapshot, BaseViewHolder> {

    private Context mContext;

    public SnapshotAdapter(Context context, List<Snapshot> data, int headerViewID) {
        super(R.layout.snapshot_list_item, data);

        View headerView = LayoutInflater.from(context).inflate(headerViewID, null, false);
        this.addHeaderView(headerView);
        this.openLoadAnimation();
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Snapshot item) {
        viewHolder.setText(R.id.snapshot_os_name_view, item.getOs())
                .setText(R.id.snapshot_description_view, new String(Base64.decode(item.getDescription(), Base64.DEFAULT)))
                .setText(R.id.snapshot_md5_view, item.getMd5())
                .setText(snapshot_purges_in, Switch.second2Any(item.getPurgesIn()))
                .setText(R.id.snapshot_size_view, Switch.b2Any(Long.parseLong(item.getSize())))
                .setText(R.id.snapshot_uncompressed_size_view, Switch.b2Any(item.getUncompressed()));
        if (item.getSticky()) {
            viewHolder.setText(R.id.snapshot_purges_in_tips, "")
                    .setText(snapshot_purges_in, mContext.getString(R.string.sticky_never_expires));
        } else {
            viewHolder.setText(R.id.snapshot_purges_in_tips, mContext.getString(R.string.str_purges_in));
        }
    }
}