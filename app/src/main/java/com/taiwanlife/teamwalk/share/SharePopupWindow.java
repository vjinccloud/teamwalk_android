package com.taiwanlife.teamwalk.share;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.taiwanlife.teamwalk.MainActivity;
import com.taiwanlife.teamwalk.R;

import org.json.JSONException;
import org.json.JSONObject;

import retrofit2.http.Url;

public class SharePopupWindow extends PopupWindow {

    //每行显示多少个
    private static final int APP_SIZE = 4;

    private View mMenuView;
    private GridView mGridView;
    private TextView mTextViewClose;
    private AppInfoAdapter mAdapter;
//    private Intent shareIntent;

    private List<AppInfo> mAppinfoList;
    private List<GridView> mGridViewList;

//    private Uri shareURI = null;
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    /**
     *
     * @param activity
     * @param shareContent 要分享的内容
     */
    public SharePopupWindow(final MainActivity activity, final String shareContent) {
        super(activity);
        LayoutInflater inflater = (LayoutInflater) activity  .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mMenuView = inflater.inflate(R.layout.share_dialog, null);
        //获取控件
        mGridView=(GridView) mMenuView.findViewById(R.id.sharePopupWindow_gridView);
        mTextViewClose=(TextView) mMenuView.findViewById(R.id.sharePopupWindow_close);
        //获取有分享功能的应用
//        shareIntent = new Intent(Intent.ACTION_SEND);
//        //shareIntent.setType("text/plain"); //纯文本
//        shareIntent.setType("text/html");
//        shareIntent.putExtra(Intent.EXTRA_TEXT, shareContent);
//        mAppinfoList = ShareUtil.getShareAppList(activity);
        mAppinfoList = ShareUtil.getShareDefaultAppList(activity);

        View shareView = mMenuView.findViewById(R.id.shareView);
        View noneView = mMenuView.findViewById(R.id.noneView);
        TextView noneView_close = mMenuView.findViewById(R.id.noneView_close);
        noneView_close.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        if(mAppinfoList.size()>0){
            shareView.setVisibility(View.VISIBLE);
            noneView.setVisibility(View.GONE);
        }else{
            shareView.setVisibility(View.GONE);
            noneView.setVisibility(View.VISIBLE);
        }

        //适配GridView
        mAdapter=new AppInfoAdapter(activity, mAppinfoList);
        mGridView.setAdapter(mAdapter);

        //修改GridView
        changeGridView(activity);

        JSONObject jsonObject = null;
        String target="";
        String title="";
        String content="";
        String image="";
        String url="";
        try {
            jsonObject = new JSONObject(shareContent);
            target=jsonObject.getString("target");
            title=jsonObject.getString("title");
            content=jsonObject.getString("content");
            image=jsonObject.getString("image");
            url=jsonObject.getString("url");
        } catch (JSONException e) {
//            e.printStackTrace();
        }

        String finalImage = image;
        String finalTitle = title;
        String finalContent = content;
        String finalUrl = url;
        mGridView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position,
                                    long id) {
                AppInfo appInfo=mAppinfoList.get(position);
                ShareUtil.RunShareContent(finalTitle, finalContent, finalImage, finalUrl, appInfo, activity);

            }
        });
        //取消按钮
        mTextViewClose.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                dismiss();
            }
        });
        //设置SelectPicPopupWindow的View
        this.setContentView(mMenuView);
        //设置SelectPicPopupWindow弹出窗体的宽
        this.setWidth(LayoutParams.FILL_PARENT);
        //设置SelectPicPopupWindow弹出窗体的高
        this.setHeight(LayoutParams.WRAP_CONTENT);
        //设置SelectPicPopupWindow弹出窗体可点击
        this.setFocusable(true);
        //设置窗口外也能点击（点击外面时，窗口可以关闭）
        this.setOutsideTouchable(true);
        //设置SelectPicPopupWindow弹出窗体动画效果
        this.setAnimationStyle(R.style.circleDialog);
        //实例化一个ColorDrawable颜色为半透明
        ColorDrawable dw = new ColorDrawable(0x00000000);
        //设置SelectPicPopupWindow弹出窗体的背景
        this.setBackgroundDrawable(dw);
    }

    /**
     * 将GridView改成单行横向布局
     */
    private void changeGridView(Context context) {
        // item宽度
        int itemWidth = dip2px(context, 100);
        // item之间的间隔
        int itemPaddingH = dip2px(context, 10);
        int size = mAppinfoList.size();
        // 计算GridView宽度
        int gridviewWidth = size * (itemWidth + itemPaddingH) -itemPaddingH;

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                gridviewWidth, LinearLayout.LayoutParams.MATCH_PARENT);
        mGridView.setLayoutParams(params);
        mGridView.setColumnWidth(itemWidth);
        mGridView.setHorizontalSpacing(itemPaddingH);
        mGridView.setStretchMode(GridView.NO_STRETCH);
        mGridView.setNumColumns(size);
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     * @param context   上下文
     * @param dpValue   dp值
     * @return  px值
     */
    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

}