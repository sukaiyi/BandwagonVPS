package com.sukaiyi.bandwagonvps.adapter;

import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.sukaiyi.bandwagonvps.R;
import com.sukaiyi.bandwagonvps.bean.Shell;
import com.sukaiyi.bandwagonvps.interfaces.ShellListener;

import java.util.List;


public class ShellListAdapter extends BaseMultiItemQuickAdapter<Shell, BaseViewHolder> {

    private ShellListener mShellListener;
    private TextWatcher mTextWatcher;

    public ShellListAdapter(ShellListener listener, List<Shell> messageWrapper) {
        super(messageWrapper);
        addItemType(Shell.TYPE_RESPONSE, R.layout.shell_response_list_item);
        addItemType(Shell.TYPE_REQUEST, R.layout.shell_request_list_item);
        mShellListener = listener;

        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().contains("\n")) {
                    mShellListener.onEnter(s.toString().replaceAll("\n",""));
                }
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
                    ((EditText) viewHolder.getView(R.id.command_editor)).removeTextChangedListener(mTextWatcher);
                } else {
                    viewHolder.setText(R.id.command_editor, shell.getMessage());
                    viewHolder.getView(R.id.command_editor).setEnabled(true);
                    ((EditText) viewHolder.getView(R.id.command_editor)).addTextChangedListener(mTextWatcher);
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