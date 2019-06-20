package com.example.foodbuddy

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.view.ViewPropertyAnimator
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AlphaAnimation
import android.widget.ProgressBar

class AnimationUtils {

    private fun fadeInAnimation(fadeInDuration: Int, fadeInDelay: Int): AlphaAnimation {
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.duration = fadeInDuration.toLong()
        fadeIn.startOffset = fadeInDelay.toLong()
        fadeIn.interpolator = AccelerateDecelerateInterpolator()
        fadeIn.fillAfter = true

        return fadeIn
    }

    private fun fadeOutAnimation(fadeOutDuration: Int, fadeOutDelay: Int): AlphaAnimation {
        val fadeOut = AlphaAnimation(1.0f, 0.0f)
        fadeOut.duration = fadeOutDuration.toLong()
        fadeOut.startOffset = fadeOutDelay.toLong()
        fadeOut.interpolator = AccelerateDecelerateInterpolator()
        fadeOut.fillAfter = true

        return fadeOut
    }

    @SuppressLint("ObjectAnimatorBinding")
    fun generatePbAnim(pb: ProgressBar, start: Int, end: Int) {
        val progressAnimator = ObjectAnimator.ofFloat(pb, "progress",
            start.toFloat(), end.toFloat())
        progressAnimator.duration = 500
        progressAnimator.start()
        pb.progress = end
    }

    fun generateFadeInAnimation(fadeInDuration: Int, fadeInDelay: Int): AlphaAnimation {
        return fadeInAnimation(fadeInDuration, fadeInDelay)
    }

    fun generateFadeOutAnimation(fadeOutDuration: Int, fadeOutDelay: Int): AlphaAnimation {
        return fadeOutAnimation(fadeOutDuration, fadeOutDelay)
    }
}