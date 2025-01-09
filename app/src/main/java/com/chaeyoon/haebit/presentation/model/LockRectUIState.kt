package com.chaeyoon.haebit.presentation.model

import androidx.annotation.DrawableRes
import com.chaeyoon.haebit.R
import com.chaeyoon.haebit.common.model.Position
import com.chaeyoon.haebit.obscura.core.LockState

sealed class LockRectUIState {
    data class LockRectProcessingState(
        @DrawableRes val drawableRes: Int,
        val position: Position
    ) : LockRectUIState()

    data class LockRectLockedState(
        @DrawableRes val drawableRes: Int,
        val position: Position,
        val visibleTimeMillis: Long
    ) : LockRectUIState()

    data object LockRectUnlockedState : LockRectUIState()

    companion object {
        fun from(lockState: LockState, touchPosition: Position): LockRectUIState =
            when (lockState) {
                LockState.LOCK_PROCESSING -> {
                    LockRectProcessingState(R.drawable.lock_region_processing,touchPosition)
                }

                LockState.LOCKED -> {
                    LockRectLockedState(R.drawable.lock_region_locked, touchPosition,2000L)
                }

                LockState.UNLOCKED -> {
                    LockRectUnlockedState
                }
            }
    }
}

