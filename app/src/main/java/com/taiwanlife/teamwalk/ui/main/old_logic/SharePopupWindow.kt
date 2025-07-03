package com.taiwanlife.teamwalk.ui.main.old_logic

import android.app.Activity
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import com.taiwanlife.teamwalk.databinding.ShareDialogBinding
import com.taiwanlife.teamwalk.ui.main.old_logic.AppInfoAdapter
import com.taiwanlife.teamwalk.utils.AppInfo
import com.taiwanlife.teamwalk.utils.ShareUtil
import org.json.JSONException
import org.json.JSONObject

class SharePopupWindow : PopupWindow {
    private lateinit var viewBinding: ShareDialogBinding
    private lateinit var appInfoList: List<AppInfo>

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int, defStyleRes: Int) : super(
        context,
        attrs,
        defStyleAttr,
        defStyleRes
    )

    constructor() : super()
    constructor(contentView: View) : super(contentView)
    constructor(width: Int, height: Int) : super(width, height)
    constructor(contentView: View, width: Int, height: Int) : super(contentView, width, height)
    constructor(contentView: View, width: Int, height: Int, focusable: Boolean) : super(
        contentView,
        width,
        height,
        focusable
    )

    fun init(activity: Activity, shareContent: String) {
        viewBinding = ShareDialogBinding.inflate(LayoutInflater.from(activity))
        contentView = viewBinding.root

        appInfoList = ShareUtil.getShareDefaultAppList(activity)

        viewBinding.noneViewClose.setOnClickListener {
            dismiss()
        }

        if (appInfoList.isNotEmpty()) {
            viewBinding.shareView.visibility = View.VISIBLE
            viewBinding.noneView.visibility = View.GONE
        } else {
            viewBinding.shareView.visibility = View.GONE
            viewBinding.noneView.visibility = View.VISIBLE
        }

        viewBinding.sharePopupWindowGridView.adapter = AppInfoAdapter(activity, appInfoList)

        val jsonObject: JSONObject
        var target = ""
        var title = ""
        var content = ""
        var image = ""
        var url = ""
        try {
            jsonObject = JSONObject(shareContent);
            target = jsonObject.getString("target");
            title = jsonObject.getString("title");
            content = jsonObject.getString("content");
            image = jsonObject.getString("image");
            url = jsonObject.getString("url");
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        val finalImage = image
        val finalTitle = title
        val finalContent = content
        val finalUrl = url

        viewBinding.sharePopupWindowGridView.setOnItemClickListener { parent, view, position, id ->
            val appInfo = appInfoList[position]
            ShareUtil.runShareContent(finalTitle, finalContent, finalImage, finalUrl, appInfo, activity)
        }
    }
}