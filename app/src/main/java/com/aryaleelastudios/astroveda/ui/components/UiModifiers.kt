package com.aryaleelastudios.astroveda.ui.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

/**
 * Clears input focus and dismisses the soft keyboard when tapping outside focused text fields.
 */
fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    this.pointerInput(Unit) {
        detectTapGestures(
            onTap = {
                focusManager.clearFocus(force = true)
                keyboardController?.hide()
            }
        )
    }
}
