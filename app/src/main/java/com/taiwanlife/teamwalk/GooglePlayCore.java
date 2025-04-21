package com.taiwanlife.teamwalk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;

public class GooglePlayCore {
    private static final String TAG = "GooglePlayCore";
    private Activity mActivity;
    private ReviewManager manager = null;
    private ReviewInfo reviewInfo = null;

    public GooglePlayCore(Activity activity){
        this.mActivity = activity;
        request();
    }

    private void request(){
        manager = ReviewManagerFactory.create(mActivity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // We can get the ReviewInfo object
                reviewInfo = task.getResult();
//                Log.i(TAG, "RequestReviewInfo: ok "+ Thread.currentThread().getId());
            } else {
                // There was some problem, log or handle the error code.
//                Log.e(TAG, "RequestReviewInfo: error "+task.getException().toString());
                reviewInfo = null;
            }
        });
    }

    public boolean canScore(){
        return reviewInfo==null?false:true;
    }

    public void scoreGooglePlay(){
        if(reviewInfo!=null){
            Log.i(TAG, "scoreGooglePlay: run");
            Task<Void> flow = manager.launchReviewFlow(mActivity, reviewInfo);
            flow.addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Log.i(TAG, "FlowReviewInfo: success");
                    //流程已完成。API不会回调告诉开发者用户是否选择了评价，以及评价结果，甚至在一些没有弹出评价框的情况中，也会进入到这里。因此，无论结果如何，我们都会继续我们的应用程序流。
                } else {
//                    Log.e(TAG, "FlowReviewInfo: error"+task.getException().toString());
                }
            });
        }
    }

    public interface GooglePlayFlowListener{
        void OnCompleteListener();
        void OnErrorListener(Exception exception);
    }
    public static void scoreGooglePlay(@NonNull Activity activity, GooglePlayFlowListener listener){
        ReviewManager manager = ReviewManagerFactory.create(activity);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                ReviewInfo reviewInfo = task.getResult();
                Task<Void> flow = manager.launchReviewFlow(activity, reviewInfo);
                flow.addOnCompleteListener(task1 -> {
                    if (listener!=null){
                        listener.OnCompleteListener();
                    }
                });
            } else {
                // There was some problem, log or handle the error code.
                //@ReviewErrorCode int reviewErrorCode = ((TaskException) task.getException()).getErrorCode();
                if (listener!=null){
                    listener.OnErrorListener(task.getException());
                }
            }
        });
    }

    public static void openGooglePlay(Context context) {
        String playPackage = "com.android.vending";
        try {
            String currentPackageName = context.getPackageName();
            if (currentPackageName != null) {
                Uri currentPackageUri = Uri.parse("market://details?id="+context.getPackageName());
                Intent intent = new Intent(Intent.ACTION_VIEW, currentPackageUri);
                intent.setPackage(playPackage);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
//            e.printStackTrace();
            Uri currentPackageUri = Uri.parse("https://play.google.com/store/apps/details?id=" + context.getPackageName());
            Intent intent = new Intent(Intent.ACTION_VIEW, currentPackageUri);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


}
