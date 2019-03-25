package com.hitasoft.materialslider;

/**
 * Created by hitasoft on 24/11/17.
 */

import android.content.Context;
import android.support.v7.widget.AppCompatSeekBar;
import android.util.AttributeSet;
import android.widget.SeekBar;

public class MaterialSeekBar extends AppCompatSeekBar implements SeekBar.OnSeekBarChangeListener {
    private OnSeekBarChangeListener mInternalListener;
    private OnSeekBarChangeListener mExternalListener;

    public MaterialSeekBar(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    public MaterialSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        setOnSeekBarChangeListener(this);
    }

    @Override
    public void setOnSeekBarChangeListener(OnSeekBarChangeListener l) {
        if (mInternalListener == null) {
            mInternalListener = l;
            super.setOnSeekBarChangeListener(l);
        } else {
            mExternalListener = l;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean b) {
        if (mExternalListener != null) {
            mExternalListener.onProgressChanged(seekBar, progress, b);
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mExternalListener != null) {
            mExternalListener.onStartTrackingTouch(seekBar);
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if (mExternalListener != null) {
            mExternalListener.onStopTrackingTouch(seekBar);
        }
    }

}