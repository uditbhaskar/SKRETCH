package com.skretch.demo

import androidx.compose.runtime.saveable.Saver

/**
 * Saves enums by name so catalog selections survive configuration changes reliably.
 * Bundle auto-save is not trustworthy for Kotlin enums inside [androidx.compose.runtime.mutableStateOf].
 */
internal inline fun <reified T : Enum<T>> enumNameSaver(): Saver<T, String> = Saver(
    save = { it.name },
    restore = { name ->
        enumValues<T>().firstOrNull { it.name == name } ?: enumValues<T>().first()
    },
)
