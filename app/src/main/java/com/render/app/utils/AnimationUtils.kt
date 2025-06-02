/*
 * File: AnimationUtils.kt
 * Created: 2025-06-02
 * Author: Vishalsnw
 * Last Modified: 2025-06-02 05:34:02 UTC
 * 
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.reader.app.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.view.ViewCompat
import androidx.core.view.ViewPropertyAnimatorCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.Transition
import androidx.transition.TransitionManager
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * Utility class for handling animations throughout the app.
 * Features:
 * - View animations
 * - Transition effects
 * - Custom interpolators
 * - Animation sequences
 */
object AnimationUtils {

    /**
     * Duration constants
     */
    const val DURATION_VERY_SHORT = 100L
    const val DURATION_SHORT = 200L
    const val DURATION_MEDIUM = 300L
    const val DURATION_LONG = 400L
    const val DURATION_VERY_LONG = 500L

    /**
     * Basic View Animations
     */
    fun fadeIn(view: View, duration: Long = DURATION_MEDIUM): ViewPropertyAnimatorCompat {
        view.alpha = 0f
        view.visibility = View.VISIBLE
        return ViewCompat.animate(view)
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
    }

    fun fadeOut(view: View, duration: Long = DURATION_MEDIUM): ViewPropertyAnimatorCompat {
        return ViewCompat.animate(view)
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { view.visibility = View.GONE }
    }

    fun slideIn(view: View, fromRight: Boolean = true, duration: Long = DURATION_MEDIUM) {
        val deltaX = if (fromRight) view.width else -view.width
        view.translationX = deltaX.toFloat()
        view.visibility = View.VISIBLE
        ViewCompat.animate(view)
            .translationX(0f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
    }

    fun slideOut(view: View, toRight: Boolean = true, duration: Long = DURATION_MEDIUM) {
        val deltaX = if (toRight) view.width else -view.width
        ViewCompat.animate(view)
            .translationX(deltaX.toFloat())
            .setDuration(duration)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { view.visibility = View.GONE }
    }

    /**
     * Advanced View Animations
     */
    fun scaleIn(view: View, duration: Long = DURATION_MEDIUM) {
        view.scaleX = 0f
        view.scaleY = 0f
        view.visibility = View.VISIBLE
        ViewCompat.animate(view)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
    }

    fun scaleOut(view: View, duration: Long = DURATION_MEDIUM) {
        ViewCompat.animate(view)
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { view.visibility = View.GONE }
    }

    fun rotateView(view: View, degrees: Float, duration: Long = DURATION_MEDIUM) {
        ViewCompat.animate(view)
            .rotation(degrees)
            .setDuration(duration)
            .setInterpolator(AccelerateDecelerateInterpolator())
    }

    /**
     * Transition Animations
     */
    fun crossFade(oldView: View, newView: View, duration: Long = DURATION_MEDIUM) {
        oldView.alpha = 1f
        newView.alpha = 0f
        newView.visibility = View.VISIBLE

        oldView.animate()
            .alpha(0f)
            .setDuration(duration)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction { oldView.visibility = View.GONE }

        newView.animate()
            .alpha(1f)
            .setDuration(duration)
            .setInterpolator(DecelerateInterpolator())
    }

    fun flipView(view: View, showFront: Boolean, duration: Long = DURATION_LONG) {
        val scale = view.context.resources.displayMetrics.density
        view.cameraDistance = 8000 * scale

        val startRotation = if (showFront) 180f else 0f
        val endRotation = if (showFront) 0f else 180f

        ObjectAnimator.ofFloat(view, View.ROTATION_Y, startRotation, endRotation).apply {
            this.duration = duration
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    /**
     * Progress Animations
     */
    fun animateProgress(
        startValue: Int,
        endValue: Int,
        duration: Long = DURATION_MEDIUM,
        onUpdate: (Int) -> Unit
    ): ValueAnimator {
        return ValueAnimator.ofInt(startValue, endValue).apply {
            this.duration = duration
            interpolator = LinearInterpolator()
            addUpdateListener { animator ->
                onUpdate(animator.animatedValue as Int)
            }
            start()
        }
    }

    /**
     * RecyclerView Animations
     */
    fun addItemAnimation(recyclerView: RecyclerView, position: Int) {
        recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.let { view ->
            view.alpha = 0f
            view.translationY = view.height.toFloat()
            view.animate()
                .alpha(1f)
                .translationY(0f)
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(DecelerateInterpolator())
                .start()
        }
    }

    fun removeItemAnimation(recyclerView: RecyclerView, position: Int, onComplete: () -> Unit) {
        recyclerView.findViewHolderForAdapterPosition(position)?.itemView?.let { view ->
            view.animate()
                .alpha(0f)
                .translationX(view.width.toFloat())
                .setDuration(DURATION_MEDIUM)
                .setInterpolator(AccelerateInterpolator())
                .withEndAction(onComplete)
                .start()
        }
    }

    /**
     * Coroutine Extensions
     */
    suspend fun View.awaitAnimation(duration: Long) = suspendCancellableCoroutine<Unit> { continuation ->
        val listener = object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                continuation.resume(Unit)
            }
        }

        animate()
            .setDuration(duration)
            .setListener(listener)

        continuation.invokeOnCancellation {
            animate().setListener(null)
        }
    }

    suspend fun Transition.awaitTransition(sceneRoot: ViewGroup) = suspendCancellableCoroutine<Unit> { continuation ->
        addListener(object : Transition.TransitionListener {
            override fun onTransitionEnd(transition: Transition) {
                continuation.resume(Unit)
                removeListener(this)
            }
            override fun onTransitionResume(transition: Transition) {}
            override fun onTransitionPause(transition: Transition) {}
            override fun onTransitionCancel(transition: Transition) {}
            override fun onTransitionStart(transition: Transition) {}
        })

        TransitionManager.beginDelayedTransition(sceneRoot, this)
    }

    /**
     * Custom Interpolators
     */
    fun createCustomBounceInterpolator(amplitude: Float = 0.2f, frequency: Float = 20f): android.view.animation.Interpolator {
        return android.view.animation.BounceInterpolator()
    }

    fun createCustomSpringInterpolator(tension: Float = 40f, friction: Float = 7f): android.view.animation.Interpolator {
        return AccelerateDecelerateInterpolator() // Replace with actual spring interpolator
    }
}
