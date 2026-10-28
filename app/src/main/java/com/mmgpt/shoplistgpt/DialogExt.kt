package com.mmgpt.shoplistgpt

import android.content.Context
import android.widget.EditText
import androidx.appcompat.app.AlertDialog

fun Context.showEditInputDialog(
    title: String,
    initialValue: String,
    onConfirmed: (String) -> Unit
) {
    val input = EditText(this).apply { setText(initialValue) }
    AlertDialog.Builder(this)
        .setTitle(title)
        .setView(input)
        .setPositiveButton(android.R.string.ok) { _, _ ->
            val text = input.text.toString().trim()
            if (text.isNotEmpty()) {
                onConfirmed(text)
            }
        }
        .setNegativeButton(android.R.string.cancel, null)
        .show()
}
