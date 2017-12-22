package com.belearn.smileyfeedback.utils;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.view.View;

import com.belearn.smileyfeedback.R;

/**
 * Created by dnlbe on 12/20/2017.
 */

public class AnimationUtils {
    public static void rotate(View view) {
        AnimatorSet set = (AnimatorSet) AnimatorInflater.loadAnimator(view.getContext(), R.animator.flip);
        set.setTarget(view);
        set.start();
    }
}
