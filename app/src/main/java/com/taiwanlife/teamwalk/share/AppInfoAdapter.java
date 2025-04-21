package com.taiwanlife.teamwalk.share;

import java.util.List;


import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.taiwanlife.teamwalk.R;

public class AppInfoAdapter extends BaseAdapter {

    private Context context;
    private List<AppInfo> mAppinfoList;
    private OnItemClickListener mOnItemClickLitener;

    public AppInfoAdapter(Context context, List<AppInfo> mAppinfoList) {
        super();
        this.context = context;
        this.mAppinfoList = mAppinfoList;
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mAppinfoList.size();
    }

    @Override
    public Object getItem(int arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @SuppressLint("NewApi")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        AppInfo appInfo = mAppinfoList.get(position);
        // 加载布局
        View view;
        ViewHolder viewHolder;
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.appinfo_item, null);
            viewHolder = new ViewHolder(view);
            // 将ViewHolder存储在View中
            view.setTag(viewHolder);
        } else {
            view = convertView;
            // 重新获取ViewHolder
            viewHolder = (ViewHolder) view.getTag();

        }
        //设置控件的值
        viewHolder.imageViewIcon.setImageDrawable(appInfo.getAppIcon());
        String name=appInfo.getAppName();
        viewHolder.textViewName.setText(name);
        return view;
    }

    class ViewHolder {
        ImageView imageViewIcon;
        TextView textViewName;

        public ViewHolder(View view) {
            this.imageViewIcon = (ImageView) view.findViewById(R.id.appinfo_item_icon);
            this.textViewName = (TextView) view.findViewById(R.id.appinfo_item_name);
        }
    }

}