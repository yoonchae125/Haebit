package com.chaeyoon.haebit.obscura.functions

internal fun nanoSecondsToSeconds(nanoSeconds:Long):Float =
    nanoSeconds/NANO_SECONDS

private const val NANO_SECONDS = 1_000_000_000f
