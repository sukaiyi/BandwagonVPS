package com.sukaiyi.bandwagonvps.adapter;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sukaiyi.bandwagonvps.R;
import com.sukaiyi.bandwagonvps.bean.Host;

import java.util.List;

/**
 * Created by sukaiyi on 2017/05/13.
 */

public class HostListAdapter extends BaseQuickAdapter<Host, BaseViewHolder> {

    public HostListAdapter(List<Host> data) {
        super(R.layout.host_list_item, data);
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Host item) {
        viewHolder.setText(R.id.host_name_view,item.getName())
                .setText(R.id.host_id_view,item.getID())
                .setText(R.id.host_plan_view,item.getPlan())
                .setText(R.id.host_email_view,item.getEmail());
    }
}