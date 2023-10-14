package com.godjango.godjangonotify20.ui.components

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import com.godjango.godjangonotify20.R

class ConfirmDialog() {
    operator fun invoke(context:Context,messageRes:Int, onConfirm:()->Unit) = AlertDialog.Builder(context)
        .setMessage(messageRes)
        .setPositiveButton(
            context.resources.getString(android.R.string.ok)
        ) { _, _ -> onConfirm() }.setNegativeButton(
            context.resources.getString(android.R.string.cancel)
        ) { dialog: DialogInterface, _: Int -> dialog.dismiss() }
        .create().show()
}