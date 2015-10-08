package com.rwidget.pileview;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.LinearInterpolator;
import android.widget.Adapter;
import android.widget.FrameLayout;

import com.rwidget.R;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;

public class PileView extends PileAdaptView implements ViewTreeObserver.OnGlobalLayoutListener {
    private static final String TAG = "PileView";
    private Adapter mAdapter;
    private View mActivePiece;
    private AdapterDataSetObserver mDataSetObserver;
    private OnSwipingListener mOnSwipingListener;

    private static final int DEFAULT_VISIBLE_SIZE = 3;
    private static final int DEFAULT_PIECE_DISTANCE = 8;
    private static final int DEFAULT_DISTANCE = DEFAULT_PIECE_DISTANCE * DEFAULT_VISIBLE_SIZE;

    private boolean isOnLayout;
    private int topPiecePosition;
    private int visibleSize = DEFAULT_VISIBLE_SIZE;
    private int pieceDistance = DEFAULT_PIECE_DISTANCE;
    private int topPieceDis = DEFAULT_DISTANCE;
    private int[] pivot;

    public PileView(Context context) {
        this(context, null);
    }

    public PileView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PileView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        getViewTreeObserver().addOnGlobalLayoutListener(this);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mAdapter == null) {
            return;
        }
        isOnLayout = true;
        final int childCount = mAdapter.getCount();
        if (childCount == 0) {
            removeAllViewsInLayout();
        } else {
            View topPiece = getChildAt(topPiecePosition);
            //Log.d(TAG, mActivePiece + "--" + topPiece + "--" + (topPiece == mActivePiece));
            if (mActivePiece != null && topPiece != null && topPiece == mActivePiece) {
                //TODO 判断是否在触摸
                //删除位置1、2，加载新的1、2
                //                Log.d(TAG, "load part....");
                removeViewsInLayout(0, topPiecePosition);
                layoutChildren(1, childCount);
            } else {
                //                Log.d(TAG, "load all....");
                //初始化，重新加载位置0、1、2
                removeAllViewsInLayout();
                layoutChildren(0, childCount);
                setActivePiece();
            }
        }
        isOnLayout = false;
    }

    private void layoutChildren(int childIndex, int childCount) {
        while (childIndex < Math.min(childCount, visibleSize)) {
            View childView = mAdapter.getView(childIndex, null, this);
            if (childView.getVisibility() != GONE) {
                populateChildView(childView, dp2px(topPieceDis - pieceDistance * childIndex), dp2px(pieceDistance * childIndex));
                topPiecePosition = childIndex;
            }
            childIndex++;
        }
    }

    private void populateChildView(View childView, int topDistance, int sideDistance) {
        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) childView.getLayoutParams();
        lp.gravity = Gravity.TOP;
        lp.topMargin = topDistance;
        lp.bottomMargin = sideDistance;
        lp.rightMargin = sideDistance;
        lp.leftMargin = sideDistance;

        addViewInLayout(childView, 0, lp, true);
        final boolean needToMeasure = childView.isLayoutRequested();
        if (needToMeasure) {
            int childWidthSpec = getChildMeasureSpec(getWidthMeasureSpec(), getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin, lp.width);
            int childHeightSpec = getChildMeasureSpec(getHeightMeasureSpec(), getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin, lp.height);
            childView.measure(childWidthSpec, childHeightSpec);
        } else {
            cleanupLayoutState(childView);
        }
        int w = childView.getMeasuredWidth();
        int h = childView.getMeasuredHeight();
        //TODO gravity判断
        int gravity = lp.gravity;
        if (gravity == -1) {
            gravity = Gravity.TOP | Gravity.START;
        }
        final int verticalGravity = gravity & Gravity.VERTICAL_GRAVITY_MASK;

        int childLeft;
        int childTop;
        childLeft = getPaddingLeft() + lp.leftMargin;
        switch (verticalGravity) {
            case Gravity.CENTER_VERTICAL:
                childTop = (getHeight() + getPaddingTop() - getPaddingBottom() - h) / 2 + lp.topMargin - lp.bottomMargin;
                break;
            case Gravity.BOTTOM:
                childTop = getHeight() - getPaddingBottom() - h - lp.bottomMargin;
                break;
            case Gravity.TOP:
            default:
                childTop = getPaddingTop() + lp.topMargin;
                break;
        }
        childView.layout(childLeft, childTop, childLeft + w, childTop + h);
    }

    //setonTouch事件，传参
    private void setActivePiece() {
        if (getChildCount() > 0) {
            mActivePiece = getChildAt(topPiecePosition);
            View pieceBody = mActivePiece.findViewById(R.id.piece);
            //TODO pivot坐标不准确，旋转时发生位移
            if (mActivePiece != null) {
                if (pieceBody != null) {
                    pivot = new int[]{mActivePiece.getLeft() + pieceBody.getMeasuredWidth() + pieceBody.getLeft(), mActivePiece.getTop() + pieceBody.getMeasuredHeight() + pieceBody.getTop()};
                } else {
                    pivot = new int[]{mActivePiece.getMeasuredWidth() + mActivePiece.getLeft() - mActivePiece.getPaddingRight(), mActivePiece.getMeasuredHeight() + mActivePiece.getTop() - mActivePiece.getPaddingBottom()};
                }
                mActivePiece.setOnTouchListener(new PileSwipingListener(pivot, this, new PileSwipingListener.SwipingListener() {
                    @Override
                    public void pileDataSetChanged() {
                        mActivePiece = null;
                        mOnSwipingListener.modifyListData();
                    }
                }));
            }
        }
    }

    @Override
    public void onGlobalLayout() {
        //TODO 锚点有问题,scale未计算
        ObjectAnimator[] animators = new ObjectAnimator[4 * getChildCount()];
        for (int i = 0, size = getChildCount(); i < size; i++) {
            animators[4 * i] = ObjectAnimator.ofFloat(getChildAt(i), "rotation", -10 * i, 0);
            animators[4 * i + 1] = ObjectAnimator.ofFloat(getChildAt(i), "pivotX", pivot[0]);
            animators[4 * i + 2] = ObjectAnimator.ofFloat(getChildAt(i), "pivotY", pivot[1]);
			//TODO 4.4 alpha 产生图层覆盖问题
            animators[4 * i + 3] = ObjectAnimator.ofFloat(getChildAt(i), "alpha", 0, 1 - (float) Math.pow(0.6, (i + 1)));
        }
        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animators);
        animatorSet.setDuration(300);
        animatorSet.setInterpolator(new LinearInterpolator());
        animatorSet.start();
    }

    public void setOnSwipingListener(OnSwipingListener listener) {
        this.mOnSwipingListener = listener;
    }

    //TODO 回调接口
    public interface OnSwipingListener {

        /**
         * 删除位置0数据，notify
         */
        void modifyListData();

    }

    @Override
    public void requestLayout() {
        if (!isOnLayout) {
            super.requestLayout();
        }
    }

    @Override
    public View getSelectedView() {
        return mActivePiece;
    }

    @Override
    public Adapter getAdapter() {
        return mAdapter;
    }

    @Override
    public void setAdapter(Adapter adapter) {
        if (mAdapter != null && mDataSetObserver != null) {
            mAdapter.unregisterDataSetObserver(mDataSetObserver);
            mDataSetObserver = null;
        }
        this.mAdapter = adapter;
        if (mAdapter != null && mDataSetObserver == null) {
            mDataSetObserver = new AdapterDataSetObserver();
            mAdapter.registerDataSetObserver(mDataSetObserver);
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new FrameLayout.LayoutParams(getContext(), attrs);
    }

    public int dp2px(int dp) {
        return (int) (0.5f + getResources().getDisplayMetrics().density * dp);
    }

    private class AdapterDataSetObserver extends DataSetObserver {
        @Override
        public void onChanged() {
            requestLayout();
        }

        @Override
        public void onInvalidated() {
            requestLayout();
        }
    }
}
