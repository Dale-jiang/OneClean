package com.kk.newcleanx.ui.common.widget

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.core.view.animation.PathInterpolatorCompat
import androidx.transition.Fade
import androidx.transition.SidePropagation
import androidx.transition.TransitionValues

class TransitionFadeForRecycler : Fade(MODE_IN) {

    init {
        interpolator = PathInterpolatorCompat.create(0f, 0f, 0.2f, 1.0f)
        propagation = SidePropagation().apply {
            setSide(Gravity.BOTTOM)
            setPropagationSpeed(1.0f)
        }
        duration = 130L
    }


    override fun onAppear(sceneRoot: ViewGroup, view: View, startValues: TransitionValues?, endValues: TransitionValues?): Animator? {
        return runCatching {
            val target = (startValues?.view ?: endValues?.view) ?: return null
            target.translationY = target.height / 2f
            val moveUp = ObjectAnimator.ofFloat(target, View.TRANSLATION_Y, target.height / 2f, 0f)
            val fadeIn = super.onAppear(sceneRoot, target, startValues, endValues) ?: return null
            AnimatorSet().apply { playTogether(moveUp, fadeIn) }
        }.getOrNull()
    }

}
