package com.sukaiyi.bandwagonvps.adapter;

import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sukaiyi.bandwagonvps.R;
import com.sukaiyi.bandwagonvps.bean.Shell;
import com.sukaiyi.bandwagonvps.interfaces.ShellListener;

import java.util.List;


public class ShellListAdapter extends BaseMultiItemQuickAdapter<Shell, BaseViewHolder> {

    private ShellListener mShellListener;
    private TextView.OnEditorActionListener mOnEditorActionListener;

    public ShellListAdapter(ShellListener listener, List<Shell> messageWrapper) {
        super(messageWrapper);
        addItemType(Shell.TYPE_RESPONSE, R.layout.shell_response_list_item);
        addItemType(Shell.TYPE_REQUEST, R.layout.shell_request_list_item);
        mShellListener = listener;

        mOnEditorActionListener = new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEND ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null &&
                                KeyEvent.KEYCODE_ENTER == event.getKeyCode() &&
                                KeyEvent.ACTION_DOWN == event.getAction())) {
                    mShellListener.onEnter(v.getText().toString());
                    return true;
                }
                return false;
            }
        };
    }

    @Override
    protected void convert(BaseViewHolder viewHolder, Shell shell) {
        switch (viewHolder.getItemViewType()) {
            case Shell.TYPE_REQUEST:
                viewHolder.setText(R.id.current_path, shell.getCurrentPath());
                if (shell.isOver()) {
                    viewHolder.setText(R.id.command_editor, shell.getMessage());
                    viewHolder.getView(R.id.command_editor).setEnabled(false);
                    ((EditText) viewHolder.getView(R.id.command_editor)).setOnEditorActionListener(null);
                } else {
                    viewHolder.setText(R.id.command_editor, shell.getMessage());
                    viewHolder.getView(R.id.command_editor).setEnabled(true);
                    ((EditText) viewHolder.getView(R.id.command_editor)).setOnEditorActionListener(mOnEditorActionListener);
                    viewHolder.getView(R.id.command_editor).requestFocus();
                }
                break;
            case Shell.TYPE_RESPONSE:
                viewHolder.setText(R.id.content, shell.getMessage());
                break;
            default:
                break;
        }
    }

    @Override
    public void addData(@NonNull Shell data) {
        super.addData(data);
        this.getRecyclerView().scrollToPosition(this.getItemCount() - 1);
    }

}