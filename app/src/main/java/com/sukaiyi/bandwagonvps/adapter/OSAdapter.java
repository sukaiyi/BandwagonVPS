package com.sukaiyi.bandwagonvps.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sukaiyi.bandwagonvps.R;

import java.util.List;

/**
 * Created by sukaiyi on 2017/05/13.
 */

public class OSAdapter extends BaseQuickAdapter<String, BaseViewHolder> {

    public OSAdapter(Context context,List<String> data, int headerViewID) {
        super(R.layout.os_list_item, data);

        View headerView = LayoutInflater.from(context).inflate(headerViewID,null,false);
        this.addHeaderView(headerView);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, String item) {
        viewHolder.setText(R.id.os_name_view, item);
    }
}