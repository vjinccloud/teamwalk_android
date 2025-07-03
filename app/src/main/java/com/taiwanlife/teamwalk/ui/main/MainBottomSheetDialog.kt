package com.taiwanlife.teamwalk.ui.main

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.taiwanlife.teamwalk.databinding.BottomSheetLayoutBinding

class MainBottomSheetDialog: BottomSheetDialog {
    private lateinit var bottomSheetLayoutBinding: BottomSheetLayoutBinding
    constructor(context: Context) : super(context)

    fun init(pickImage: () -> Unit, takePhotoOnClick: () -> Unit, pickFile: () -> Unit, cancelCallback: () -> Unit) {
        bottomSheetLayoutBinding = BottomSheetLayoutBinding.inflate(layoutInflater)

        setContentView(bottomSheetLayoutBinding.root)

        bottomSheetLayoutBinding.chooseImage
            .setOnClickListener(View.OnClickListener { v: View? ->
                dismiss()
                pickImage()
            })

        bottomSheetLayoutBinding.takePhoto
            .setOnClickListener(View.OnClickListener { v: View? ->
                dismiss()
                takePhotoOnClick.invoke()
            })

        bottomSheetLayoutBinding.chooseFile
            .setOnClickListener(View.OnClickListener { v: View? ->
                dismiss()
                pickFile()
            })

        bottomSheetLayoutBinding.btnCancel
            .setOnClickListener(View.OnClickListener { v: View? ->
                dismiss()
                cancelCallback()
            })

        setOnCancelListener(DialogInterface.OnCancelListener { dialog: DialogInterface? ->
            cancelCallback()
        })
    }
}