package com.example.summit_climb.ui

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/**
 * Displays a reusable confirmation dialog with Confirm and Cancel buttons.
 *
 * Shows a title and message explaining the action before the user confirms
 * or dismisses it.
 *
 * @param title heading displayed at the top of the dialog
 * @param message message explaining the action
 * @param confirmText text displayed on the confirm button
 * @param cancelText text displayed on the cancel button
 * @param onConfirm action performed when the confirm button is clicked
 * @param onDismiss action performed when the dialog is cancelled or dismissed
 */

@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,

        title = {
            Text(text = title)
        },

        confirmButton = {
            TextButton(
                onClick = onConfirm
            ) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(text = cancelText)
            }
        }
    )
}