package com.taiwanlife.teamwalk.ui.common

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.taiwanlife.teamwalk.databinding.DialogCommonBinding
import com.taiwanlife.teamwalk.utils.DeviceType

class CommonDialog : Dialog {
    constructor(context: Context) : super(context)
    constructor(context: Context, themeResId: Int) : super(context, themeResId)
    constructor(
        context: Context,
        cancelable: Boolean,
        cancelListener: DialogInterface.OnCancelListener?
    ) : super(context, cancelable, cancelListener)

    private lateinit var binding: DialogCommonBinding

    fun oneButtonInit(
        title: String,
        body: String,
        @DrawableRes image: Int,
        showButtons: Boolean,
        canceledOnTouchOutside: Boolean,
        text: String? = null,
        onClick: (() -> Unit)? = null
    ) {
        twoButtonInit(
            title,
            body,
            image,
            showButtons,
            canceledOnTouchOutside,
            null,
            null,
            text,
            onClick
        )
    }

    fun twoButtonInit(
        title: String,
        body: String,
        @DrawableRes image: Int,
        showButtons: Boolean,
        canceledOnTouchOutside: Boolean,
        positiveText: String? = null,
        positiveOnClick: (() -> Unit)? = null,
        negativeText: String?,
        negativeOnClick: (() -> Unit)?
    ) {
        binding = DialogCommonBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window?.setGravity(Gravity.CENTER)
        window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        setCancelable(showButtons)
        setCanceledOnTouchOutside(canceledOnTouchOutside)

        binding.alertTitle.text = title
        binding.alertBody.text = body

        val drawable = ContextCompat.getDrawable(context, image)
        binding.alertImage.setImageDrawable(drawable)

        var buttonCount = 0
        if (positiveText != null) {
            binding.alertButtonPositive.text = positiveText
            binding.alertButtonPositive.setOnClickListener {
                positiveOnClick?.invoke()
                dismiss()
            }
            binding.alertButtonPositive.visibility = View.VISIBLE
            buttonCount++
        }
        if (negativeText != null) {
            binding.alertButtonNegative.text = negativeText
            binding.alertButtonNegative.setOnClickListener {
                negativeOnClick?.invoke()
                dismiss()
            }
            buttonCount++
        }
        if(buttonCount > 1) {
            binding.divider.visibility = View.VISIBLE
        } else {
            binding.divider.visibility = View.GONE
        }

        if (!showButtons) {
            binding.alertButtonGroup.visibility = View.GONE
        }
    }
}