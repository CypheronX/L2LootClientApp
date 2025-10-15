package com.l2loot.ui.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager

/**
 * Modifier that clears focus from all text fields when the user clicks outside of them.
 * This provides a better UX by unfocusing inputs when clicking on empty space.
 * 
 * Apply this to a parent container (like NavHost or Screen root) to enable
 * click-outside-to-unfocus behavior for all child text fields.
 * 
 * This works by listening for pointer events in the Final pass, which means
 * child elements have already had a chance to consume the event. If a child
 * consumes the event (like a TextField or Button), focus won't be cleared.
 */
fun Modifier.clearFocusOnClick(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    
    this.pointerInput(Unit) {
        awaitPointerEventScope {
            while (true) {
                val event = awaitPointerEvent(PointerEventPass.Final)
                
                if (event.type == PointerEventType.Press) {
                    val wasConsumed = event.changes.any { it.isConsumed }
                    if (!wasConsumed) {
                        focusManager.clearFocus()
                    }
                }
            }
        }
    }
}

