package com.sukaiyi.bandwagonvps.adapter;

import android.content.Context;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sukaiyi.bandwagonvps.R;
import com.sukaiyi.bandwagonvps.bean.File;
import com.sukaiyi.bandwagonvps.utils.Switch;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FileListAdapter extends BaseQuickAdapter<File, BaseViewHolder> {

    private Context mContext;

    public FileListAdapter(Context context, List<File> data) {
        super(R.layout.file_list_item, data);
        mContext = context;
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, File item) {
        viewHolder.setText(R.id.file_name, item.getFileName())
                .setText(R.id.file_info, item.getInfo());

        ImageView iconView = viewHolder.getView(R.id.file_icon);
        if (item.isDirectory()) {
            iconView.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_folder_cyan_48dp, null));
            viewHolder.setText(R.id.file_type, "目录");
        } else if (item.isLink()) {
            iconView.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_link_cyan_48dp, null));
            viewHolder.setText(R.id.file_type, item.getLinkPath());
        } else {
            iconView.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_insert_drive_file_cyan_48dp, null));
            viewHolder.setText(R.id.file_type, Switch.b2Any(item.getSize()));
        }
    }

    public void sort() {
        Collections.sort(getData(), new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                if (o1.isDirectory() && !o2.isDirectory()) {
                    return -1;
                } else if (!o1.isDirectory() && o2.isDirectory()) {
                    return 1;
                } else {
                    return o1.getFileName().compareTo(o2.getFileName());
                }
            }
        });
        this.notifyDataSetChanged();
    }


}