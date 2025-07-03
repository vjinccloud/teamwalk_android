package com.taiwanlife.teamwalk.ui.main.old_logic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.recyclerview.widget.RecyclerView
import com.taiwanlife.teamwalk.databinding.AppinfoItemBinding
import com.taiwanlife.teamwalk.utils.AppInfo

class AppInfoAdapter(
    private val context: Context,
    private val appInfoList: List<AppInfo>,
) : BaseAdapter() {
    //    override fun getViewBinding(
//        layoutInflater: LayoutInflater,
//        parent: ViewGroup
//    ): AppinfoItemBinding {
//        return AppinfoItemBinding.inflate(layoutInflater, parent, false)
//    }
//
//    override fun onBindViewBinding(
//        viewBinding: AppinfoItemBinding,
//        position: Int
//    ) {
//        val appInfo = appInfoList[position]
//        viewBinding.appinfoItemIcon.setImageDrawable(appInfo.appIcon)
//        viewBinding.appinfoItemName.text = appInfo.appName
//    }
    override fun getCount(): Int {
        return appInfoList.size
    }

    override fun getItem(index: Int): Any? {
        return appInfoList[index];
    }

    override fun getItemId(index: Int): Long {
        return index.toLong()
    }

    override fun getView(
        position: Int,
        convertView: View?,
        parent: ViewGroup
    ): View {
        val view: View
        val holder: RecyclerView.ViewHolder
        val layoutInflater = LayoutInflater.from(parent.context)
        val viewBinding: AppinfoItemBinding

        if (convertView == null) {
            // 如果沒有可重複利用的 View，膨脹新的並創建綁定對象
            viewBinding = AppinfoItemBinding.inflate(layoutInflater, parent, false)
            // 將綁定對象儲存在 View 的 tag 中，以便下次重複利用
            viewBinding.root.tag = viewBinding
        } else {
            // 從 tag 中取出綁定對象
            viewBinding = convertView.tag as AppinfoItemBinding
        }
        val appInfo = appInfoList[position]
        viewBinding.appinfoItemIcon.setImageDrawable(appInfo.appIcon)
        viewBinding.appinfoItemName.text = appInfo.appName

        return viewBinding.root
    }
}