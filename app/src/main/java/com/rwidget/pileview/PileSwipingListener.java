package com.rwidget.pileview;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.AnimatorListenerAdapter;
import com.nineoldandroids.animation.AnimatorSet;
import com.nineoldandroids.animation.ObjectAnimator;
import com.nineoldandroids.view.ViewHelper;

public class PileSwipingListener implements View.OnTouchListener {
    private float mDegree;
    private float rawX = 0;
    private float x = 0;
    private int[] pivot;
    private SwipingListener mSwipingListener;
    private ViewGroup parent;

    public PileSwipingListener(int[] pivot, ViewGroup parent, SwipingListener swipingListener) {
        this.pivot = pivot;
        this.parent = parent;
        this.mSwipingListener = swipingListener;
    }

    @Override
    public boolean onTouch(final View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                rawX = event.getX();
                break;
            case MotionEvent.ACTION_MOVE:
                //TODO 反向滑动，退回上个移除的piece
                x = event.getX();
                if (x < rawX) {
                    mDegree = -0.7f * 90 * (1 - (float) x / rawX);
                    pieceRotate(v, mDegree, 30);
                }
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                if (x < rawX) {
                    AnimatorSet set = new AnimatorSet();
                    View bottomView = parent.getChildAt(0);
                    View midView = parent.getChildAt(1);
                    //TODO 移除第一个，加入最后一个
                    set.playTogether(
                            ObjectAnimator.ofFloat(bottomView, "translationX", bottomView.getWidth(), 0),
                            ObjectAnimator.ofFloat(bottomView, "alpha", 0.5f),
                            ObjectAnimator.ofFloat(midView, "rotation", 0),
                            ObjectAnimator.ofFloat(midView, "alpha", 0.7f),
                            ObjectAnimator.ofFloat(v, "translationX", -v.getWidth()));
                    set.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            mSwipingListener.pileDataSetChanged();
                        }
                    });
                    set.setDuration(300).start();
                }
                break;
        }
        return true;
    }

    //旋转卡片
    public void pieceRotate(View v, float degree, long duration) {
        //TODO 没有设置Interpolator方法
        ViewHelper.setPivotX(v, pivot[0]);
        ViewHelper.setPivotY(v, pivot[1]);
        ViewHelper.setRotation(v, degree);
        for (int size = parent.getChildCount() - 1, i = size; i > 0; i--) {
            ViewHelper.setPivotX(parent.getChildAt(i), pivot[0]);
            ViewHelper.setPivotY(parent.getChildAt(i), pivot[1]);
            ViewHelper.setRotation(parent.getChildAt(i), (float) (2 * i - 1) / 10 * degree);
        }
        //TODO 旋转锚点设置有延迟
        //        ObjectAnimator[] animators = new ObjectAnimator[3 * (parent.getChildCount() - 1)];
        //        animators[0] = ObjectAnimator.ofFloat(v, "rotation", degree);
        //        animators[1] = ObjectAnimator.ofFloat(v, "pivotX", pivot[0]);
        //        animators[2] = ObjectAnimator.ofFloat(v, "pivotY", pivot[1]);
        //        for (int i = 1, size = parent.getChildCount() - 1; i < size; i++) {
        //            animators[3 * i] = ObjectAnimator.ofFloat(parent.getChildAt(i), "rotation", 0.3f * i * degree);
        //            animators[3 * i + 1] = ObjectAnimator.ofFloat(parent.getChildAt(i), "pivotX", pivot[0]);
        //            animators[3 * i + 2] = ObjectAnimator.ofFloat(parent.getChildAt(i), "pivotY", pivot[1]);
        //        }
        //        AnimatorSet animatorSet = new AnimatorSet();
        //        animatorSet.playTogether(animators);
        //        animatorSet.setDuration(duration);
        //        animatorSet.setInterpolator(new LinearInterpolator());
        //        animatorSet.start();
    }


    protected interface SwipingListener {
        void pileDataSetChanged();
    }
}
