package com.taiwanlife.teamwalk.utils

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.text.TextUtils
import androidx.annotation.DrawableRes

object AlertDialogManager {
    fun getAlertDialog(
        context: Context, message: String, isCancelable: Boolean, shouldShow: Boolean,
        positiveText: String?, positiveOnClick: (() -> Unit)?,
        negativeText: String? = null, negativeOnClick: (() -> Unit)? = null,
        @DrawableRes icon: Int? = null
    ): AlertDialog {
        val builder = AlertDialog.Builder(context)
        builder.setMessage(message)
        builder.setCancelable(isCancelable)

        if (!TextUtils.isEmpty(positiveText)) {
            builder.setPositiveButton(positiveText, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (positiveOnClick != null) {
                        positiveOnClick()
                    } else {
                        dialog?.dismiss()
                    }
                }
            })
        }
        if (!TextUtils.isEmpty(negativeText)) {
            builder.setNegativeButton(negativeText, object : DialogInterface.OnClickListener {
                override fun onClick(dialog: DialogInterface?, which: Int) {
                    if (negativeOnClick != null) {
                        negativeOnClick()
                    } else {
                        dialog?.dismiss()
                    }
                }
            })
        }
        if(icon != null) {
            builder.setIcon(icon)
        }
        val dialog: AlertDialog = builder.create()
        if (shouldShow) {
            dialog.show()
        }
        return dialog
    }
}