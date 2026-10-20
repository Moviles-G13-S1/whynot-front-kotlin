package com.example.whynotkotlin.controller

import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope

/**
 * Keeps a controller's shared stream running for the rest of the test, the way
 * a screen does while it is on display.
 */
fun <T> TestScope.watch(stream: StateFlow<T>): StateFlow<T> {
    backgroundScope.launch { stream.collect {} }
    return stream
}
