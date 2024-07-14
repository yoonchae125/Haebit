package com.chaeyoon.haebit.obscura.view.animator

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.animation.addListener
import com.chaeyoon.haebit.obscura.utils.constants.CameraValue
import com.chaeyoon.haebit.obscura.utils.constants.FractionCameraValue
import com.chaeyoon.haebit.obscura.utils.constants.NormalCameraValue
import com.chaeyoon.haebit.obscura.utils.extensions.toOneDecimalPlaces

object CenterValueAnimator {
    fun getAnimator(
        targetView: TextView,
        currentCameraValue: CameraValue?,
        targetCameraValue: CameraValue
    ): Animator {
        targetView.clearAnimation()
        targetView.text = currentCameraValue?.getText()

        if (currentCameraValue?.type != targetCameraValue.type) {
            return fadeAnimator(
                targetView = targetView,
                text = targetCameraValue.getText()
            )
        }

        val getText: (String) -> String = { animValue ->
            targetCameraValue.prefix + animValue + targetCameraValue.suffix
        }

        return when {
            currentCameraValue is FractionCameraValue && targetCameraValue is FractionCameraValue -> {
                intValueAnimator(
                    targetView = targetView,
                    from = currentCameraValue.denominator,
                    to = targetCameraValue.denominator,
                    getText = getText
                )
            }

            currentCameraValue is NormalCameraValue && targetCameraValue is NormalCameraValue -> {
                if (!currentCameraValue.decimal && !targetCameraValue.decimal) {
                    intValueAnimator(
                        targetView = targetView,
                        from = currentCameraValue.value.toInt(),
                        to = targetCameraValue.value.toInt(),
                        getText = getText
                    )
                } else {
                    if (targetCameraValue.decimal) {
                        floatValueAnimator(
                            targetView = targetView,
                            from = currentCameraValue.value,
                            to = targetCameraValue.value
                        ) { animValue ->
                            targetCameraValue.suffix + animValue + targetCameraValue.prefix
                        }
                    } else {
                        intValueAnimator(
                            targetView = targetView,
                            from = currentCameraValue.value.toInt(),
                            to = targetCameraValue.value.toInt(),
                            getText = getText
                        )
                    }
                }
            }

            currentCameraValue is NormalCameraValue && targetCameraValue is FractionCameraValue ||
                    currentCameraValue is FractionCameraValue && targetCameraValue is NormalCameraValue -> {
                fadeAnimator(
                    targetView = targetView,
                    text = targetCameraValue.getText()
                )
            }

            else -> {
                error("not available case")
            }
        }
    }

    private fun fadeAnimator(targetView: TextView, text: String): Animator {
        val fadeOut = ObjectAnimator.ofFloat(targetView, "alpha", 1f, 0f)
        val fadeIn = ObjectAnimator.ofFloat(targetView, "alpha", 0f, 1f)

        fadeOut.duration = 150
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.duration = 150
        fadeIn.interpolator = DecelerateInterpolator()
        fadeOut.addListener(onEnd = {
            targetView.text = text
            fadeIn.start()
        })

        return fadeOut
    }

    private fun floatValueAnimator(
        targetView: TextView,
        from: Float,
        to: Float,
        getText: (String) -> String
    ): ValueAnimator {
        return ValueAnimator.ofFloat(
            from.toOneDecimalPlaces(),
            to.toOneDecimalPlaces()
        ).also {
            it.addUpdateListener { anim ->
                val animatedValue =
                    (anim.animatedValue as Float).toOneDecimalPlaces()
                targetView.text = getText(animatedValue.toString())
            }
        }
    }

    private fun intValueAnimator(
        targetView: TextView,
        from: Int,
        to: Int,
        getText: (String) -> String
    ): ValueAnimator {
        return ValueAnimator.ofInt(from, to).also {
            it.addUpdateListener { anim ->
                targetView.text = getText(anim.animatedValue.toString())
            }
        }
    }
}