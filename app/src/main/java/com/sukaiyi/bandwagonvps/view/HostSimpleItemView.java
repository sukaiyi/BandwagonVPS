package com.sukaiyi.bandwagonvps.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.sukaiyi.bandwagonvps.R;


/**
 * Created by sukaiyi on 2017/05/23.
 */

public class HostSimpleItemView extends LinearLayout {

    private TextView mTitleView;
    private TextView mValueView;

    private String mTitle;
    private String mValue;

    public HostSimpleItemView(Context context) {
        super(context);
        getAttrs(context,null);
        initView(context);
    }

    public HostSimpleItemView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        getAttrs(context,attrs);
        initView(context);
    }

    public HostSimpleItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getAttrs(context,attrs);
        initView(context);
    }

    public HostSimpleItemView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        getAttrs(context,attrs);
        initView(context);
    }

    private void getAttrs(Context context, AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.HostItemAttr);
        mTitle = ta.getString(R.styleable.HostItemAttr_itemTitle);
        mValue = ta.getString(R.styleable.HostItemAttr_itemValue);
        ta.recycle();
    }

    private void initView(Context context){
        mTitleView = new TextView(context);
        mValueView = new TextView(context);
        mValueView.setTextColor(Color.BLACK);
        mValueView.setTextSize(15.0f);

        this.setOrientation(LinearLayout.VERTICAL);
        this.addView(mTitleView);
        this.addView(mValueView);

        mTitleView.setText(mTitle);
        mValueView.setText(mValue);
    }

    public void setValue(String value){
        this.mValue = value;
        mValueView.setText(value);
    }

    public void setTitle(String title){
        this.mTitle = title;
        mTitleView.setText(title);
    }
}
