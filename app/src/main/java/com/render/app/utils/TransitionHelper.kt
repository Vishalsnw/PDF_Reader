/*
 * File: TransitionHelper.kt
 * Created: 2025-06-03
 * Author: Vishalsnw
 * Last Modified: 2025-06-03 05:02:57 UTC
 *
 * Copyright (c) 2025 Your App Name
 * Licensed under the MIT license.
 */

package com.yourapp.readers.utils

import android.content.Context
import android.view.View
import android.view.animation.AnimationUtils
import androidx.fragment.app.FragmentTransaction
import androidx.viewpager2.widget.ViewPager2
import kotlin.math.abs

object TransitionHelper {

    /**
     * Applies fade transition animation to a view
     *
     * @param context The context to load animation resources
     * @param view The view to animate
     */
    fun applyFadeTransition(context: Context, view: View) {
        val fadeTransition = AnimationUtils.loadAnimation(context, R.anim.fade_transition)
        view.startAnimation(fadeTransition)
    }

    /**
     * Applies fade transition to ViewPager2
     *
     * @param viewPager2 The ViewPager2 instance to apply the transition to
     */
    fun applyViewPagerFadeTransition(viewPager2: ViewPager2) {
        viewPager2.setPageTransformer { page, position ->
            when {
                position < -1 || position > 1 -> {
                    page.alpha = 0f
                }
                position <= 0 -> {
                    page.alpha = 1 + position
                    page.scaleX = 1 - abs(position * 0.05f)
                    page.scaleY = 1 - abs(position * 0.05f)
                }
                else -> {
                    page.alpha = 1 - position
                    page.scaleX = 1 - abs(position * 0.05f)
                    page.scaleY = 1 - abs(position * 0.05f)
                }
            }
        }
    }

    /**
     * Applies fade transition animations to FragmentTransaction
     *
     * @param transaction The FragmentTransaction to apply animations to
     */
    fun applyFragmentFadeTransition(transaction: FragmentTransaction) {
        transaction.setCustomAnimations(
            R.anim.fade_transition,
            R.anim.fade_transition,
            R.anim.fade_transition,
            R.anim.fade_transition
        )
    }
}
