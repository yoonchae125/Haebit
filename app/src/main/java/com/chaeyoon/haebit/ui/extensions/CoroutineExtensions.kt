package com.chaeyoon.haebit.ui.extensions

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

internal inline fun LifecycleOwner.launchAndRepeatOnLifecycle(
    state: Lifecycle.State = Lifecycle.State.STARTED,
    crossinline block: suspend CoroutineScope.() -> Unit
) {
    lifecycleScope.launch {
        lifecycle.repeatOnLifecycle(state) {
            block()
        }
    }
}

inline fun <T> Flow<T>.launchAndCollect(
    coroutineScope: CoroutineScope,
    crossinline block: suspend (T) -> Unit
) = coroutineScope.launch {
    collect {
        block(it)
    }
}