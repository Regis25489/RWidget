package com.rwidget.pileview;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AdapterView;

public abstract class PileAdaptView extends AdapterView {
    private int widthMeasureSpec, heightMeasureSpec;

    public PileAdaptView(Context context) {
        super(context);
    }

    public PileAdaptView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PileAdaptView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.widthMeasureSpec = widthMeasureSpec;
        this.heightMeasureSpec = heightMeasureSpec;
    }

    public int getWidthMeasureSpec() {
        return widthMeasureSpec;
    }

    public int getHeightMeasureSpec() {
        return heightMeasureSpec;
    }

    @Override
    public void setSelection(int i) {
        throw new UnsupportedOperationException("Not supported!不支持该操作！");
    }
}
