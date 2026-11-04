package com.example.summit_climb

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

/** Purpose: a reusable confirmation dialog with Confirm and Cancel buttons.
*
 * @param title The dialog heading
 * @param message The message explaining the action
 * @param confirmText Text displayed on the confirm button
 * @param canceltext Text displayed on the cancel button
 * @param onConfirm Called when the user confirms
 * @param onDismiss Called when the user cancels or dismisses the dialog
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