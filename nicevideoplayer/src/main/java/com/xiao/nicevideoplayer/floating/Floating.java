package com.xiao.nicevideoplayer.floating;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.graphics.PointF;
import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.FrameLayout;

import com.xiao.nicevideoplayer.NiceUtil;

/**
 * 2017/12/1.
 * github:[https://github.com/jacky1234]
 *
 * @author jackyang
 */

public class Floating {
    private Activity mActivity;
    private ValueAnimator mCurrentAnimator;
    private View mView;

    public Floating(Activity activity) {
        if (activity == null) {
            throw new NullPointerException("Activity should not be null");
        }
        this.mActivity = activity;
    }

    public void start(final View anchor, final OnFloatingDelegate delegate) {
        ViewGroup rootView = (ViewGroup) mActivity.findViewById(Window.ID_ANDROID_CONTENT);
        int[] location = new int[2];
        rootView.getLocationOnScreen(location);
        this.mView = anchor;        //用于clear

        Rect rectAnchor = new Rect();
        anchor.getGlobalVisibleRect(rectAnchor);
        rectAnchor.offset(-location[0], -location[1]);
        if (delegate != null) {
            delegate.onAnchorRectReach();
        }

        final FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(anchor.getWidth(), anchor.getHeight());
        lp.leftMargin = rectAnchor.left;
        lp.topMargin = rectAnchor.top;
        rootView.addView(anchor, lp);

        int screenWidth = NiceUtil.getScreenWidth(mActivity);
        int scrrenHeight = NiceUtil.getScreenHeight(mActivity);
        int padding = NiceUtil.dp2px(mActivity, 8f);
        final int targetWidth = (int) (screenWidth * 0.6f);
        final int targetHeight = (int) (screenWidth * 0.6f * 9f / 16f);
        final int anchorWidth = anchor.getWidth();
        final int anchorHeight = anchor.getHeight();
        Rect rectTarget = new Rect(
                screenWidth - targetWidth - padding,
                scrrenHeight - targetHeight - padding,
                screenWidth - padding,
                scrrenHeight - padding
        );
        rectTarget.offset(-location[0], -location[1]);

        PointF pointF0 = new PointF(rectAnchor.left, rectAnchor.top);
        PointF pointF1 = new PointF(rectTarget.left, rectTarget.top);
        if (mCurrentAnimator != null && mCurrentAnimator.isRunning()) {
            mCurrentAnimator.cancel();
        }
        mCurrentAnimator = ValueAnimator.ofObject(new PointFEvaluator(), pointF0, pointF1);
        mCurrentAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                if (delegate != null) {
                    delegate.onAninmationEnd();
                }
            }
        });
        mCurrentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                PointF current = (PointF) animation.getAnimatedValue();
                lp.leftMargin = (int) current.x;
                lp.topMargin = (int) current.y;
                lp.width = (int) (anchorWidth + (animation.getAnimatedFraction() * (targetWidth - anchorWidth) * anchorWidth) / anchorWidth);
                lp.height = (int) (anchorHeight + (animation.getAnimatedFraction() * (targetHeight - anchorHeight) * anchorHeight) / anchorHeight);
                anchor.setLayoutParams(lp);
                anchor.requestLayout();
            }
        });
        mCurrentAnimator.setDuration(500).start();
    }

    public void clear() {
        ViewGroup rootView = (ViewGroup) mActivity.findViewById(Window.ID_ANDROID_CONTENT);
        if (mView != null && rootView.indexOfChild(mView) != -1) {
            rootView.removeView(mView);
            mView = null;
        }
    }


    public interface OnFloatingDelegate {
        void onAninmationEnd();

        void onAnchorRectReach();
    }

}
