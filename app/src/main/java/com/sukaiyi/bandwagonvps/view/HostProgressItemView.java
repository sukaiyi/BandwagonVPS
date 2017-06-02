package com.sukaiyi.bandwagonvps.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.sukaiyi.bandwagonvps.R;


/**
 * Created by sukaiyi on 2017/05/23.
 */

public class HostProgressItemView extends LinearLayout {

    private TextView mTitleView;
    private TextView mTipsView;
    private TextView mValueView;

    private ProgressBar mProgressBar;

    private String mTitle;
    private String mValue;
    private String mTips;

    private long mTotal;
    private long mProgress;

    public HostProgressItemView(Context context) {
        super(context);
        getAttrs(context, null);
        initView(context);
    }

    public HostProgressItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context, attrs);
        initView(context);
    }

    public HostProgressItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context, attrs);
        initView(context);
    }

    public HostProgressItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAttrs(context, attrs);
        initView(context);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HostItemAttr);
        mTitle = ta.getString(R.styleable.HostItemAttr_itemTitle);
        mValue = ta.getString(R.styleable.HostItemAttr_itemValue);
        mTips = ta.getString(R.styleable.HostItemAttr_itemTips);
        ta.recycle();
    }

    private void initView(Context context) {
        mTitleView = new TextView(context);
        mValueView = new TextView(context);
        mValueView.setTextColor(Color.BLACK);
        mValueView.setTextSize(15.0f);
        mProgressBar = new ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal);
        mProgressBar.setMinimumHeight(2);
        mProgressBar.setBackgroundColor(Color.argb(0, 0, 0, 0));
        mTipsView = new TextView(context);
        mTipsView.setVisibility(View.GONE);

        this.setOrientation(LinearLayout.VERTICAL);
        this.addView(mTitleView);
        this.addView(mValueView);
        this.addView(mProgressBar);
        this.addView(mTipsView);

        mTitleView.setText(mTitle);
        mTipsView.setText(mTips);
        mValueView.setText(mValue);
    }

    public void setTips(String tips) {
        this.mTips = tips;
        if (TextUtils.isEmpty(tips)) {
            mTipsView.setVisibility(View.GONE);
        } else {
            mTipsView.setVisibility(View.VISIBLE);
        }
        mTipsView.setText(tips);
    }

    public void setTitle(String title) {
        this.mTitle = title;
        mTitleView.setText(title);
    }

    public void setValue(String value) {
        this.mValue = value;
        mValueView.setText(value);
    }

    public void setProgress(int progress) {
        mProgressBar.setProgress(progress);
    }

    public long getTotal() {
        return mTotal;
    }

    public void setTotal(long total) {
        mTotal = total;
    }

    public long getProgress() {
        return mProgress;
    }

    public void setProgress(long progress) {
        mProgress = progress;
        mProgressBar.setMax(100);
        mProgressBar.setProgress((int) (progress * 100 / mTotal));
    }

    public String getTitle() {
        return mTitle;
    }

    public String getValue() {
        return mValue;
    }

    public String getTips() {
        return mTips;
    }

}
