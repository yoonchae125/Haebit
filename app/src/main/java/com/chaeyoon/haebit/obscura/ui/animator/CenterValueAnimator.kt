package com.chaeyoon.haebit.obscura.ui.animator

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.TextView
import androidx.core.animation.addListener
import com.chaeyoon.haebit.obscura.model.CameraValue
import com.chaeyoon.haebit.obscura.ui.model.CameraValueType
import com.chaeyoon.haebit.obscura.ui.model.converter.getText
import com.chaeyoon.haebit.obscura.utils.extensions.hasDecimalPart
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
            val space = if (targetCameraValue.isFraction) " " else ""
            targetCameraValue.getPrefix() + space + animValue + targetCameraValue.getSuffix()
        }

        return when {
            currentCameraValue.isFraction && targetCameraValue.isFraction -> {
                intValueAnimator(
                    targetView = targetView,
                    from = currentCameraValue.value.toInt(),
                    to = targetCameraValue.value.toInt(),
                    getText = getText
                )
            }

            !currentCameraValue.isFraction && !targetCameraValue.isFraction -> {
                if (!currentCameraValue.isDecimal() && !targetCameraValue.isDecimal()) {
                    intValueAnimator(
                        targetView = targetView,
                        from = currentCameraValue.value.toInt(),
                        to = targetCameraValue.value.toInt(),
                        getText = getText
                    )
                } else {
                    if (targetCameraValue.isDecimal()) {
                        floatValueAnimator(
                            targetView = targetView,
                            from = currentCameraValue.value,
                            to = targetCameraValue.value,
                            getText = getText
                        )
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

            else -> {
                fadeAnimator(
                    targetView = targetView,
                    text = targetCameraValue.getText()
                )
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

    private fun CameraValue.getPrefix(): String = when (type) {
        CameraValueType.APERTURE -> "ƒ"

        CameraValueType.SHUTTER_SPEED -> {
            if (isFraction) {
                "¹⁄"
            } else {
                ""
            }
        }

        CameraValueType.ISO -> ""
    }

    private fun CameraValue.getSuffix(): String = when (type) {
        CameraValueType.APERTURE -> ""

        CameraValueType.SHUTTER_SPEED -> "s"

        CameraValueType.ISO -> ""
    }

    private fun CameraValue.isDecimal(): Boolean {
        return when (type) {
            CameraValueType.APERTURE -> value.hasDecimalPart()
            else -> false
        }
    }
}