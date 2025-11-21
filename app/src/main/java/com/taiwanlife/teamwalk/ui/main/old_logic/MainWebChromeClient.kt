package com.taiwanlife.teamwalk.ui.main.old_logic

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.net.Uri
import android.webkit.ConsoleMessage
import android.webkit.JsResult
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import com.taiwanlife.teamwalk.R
import com.taiwanlife.teamwalk.ui.main.old_logic.MainWebViewJava.MainWebViewJavaCallback
import com.taiwanlife.teamwalk.utils.WebViewFileWrapper

class MainWebChromeClient(
    private val context: Context,
    private val mainWebViewJavaCallback: MainWebViewJavaCallback
) : WebChromeClient() {

    //        // For Android < 3.0
    //        public void openFileChooser(ValueCallback<Uri> valueCallback) {
    //            uploadMessage = valueCallback;
    //            openImageChooserActivity();
    //        }
    //
    //        // For Android  >= 3.0
    //        public void openFileChooser(ValueCallback valueCallback, String acceptType) {
    //            uploadMessage = valueCallback;
    //            openImageChooserActivity();
    //        }
    //
    //        //For Android  >= 4.1
    //        public void openFileChooser(ValueCallback<Uri> valueCallback, String acceptType, String capture) {
    //            uploadMessage = valueCallback;
    //            openImageChooserActivity();
    //        }
    // For Android >= 5.0

    override fun onShowFileChooser(
        webView: WebView?,
        filePathCallbackParam: ValueCallback<Array<Uri>>,
        fileChooserParams: FileChooserParams
    ): Boolean {
//            uploadMessageAboveL = filePathCallback;
//            openImageChooserActivity();
//            return true;
//            if (filePathCallback != null) {
//                filePathCallback.onReceiveValue(null);
//            }
//            filePathCallback = filePathCallbackParam;

        // 準備選項清單

        val selectOptions: Array<String> = arrayOf<String>("選擇圖片", "拍照", "檔案")
        mainWebViewJavaCallback.showBottomSheetDialog(
            WebViewFileWrapper(
                filePathCallbackParam,
                fileChooserParams
            )
        ) // 呼叫下方自訂的方法
        return true // 已自行處理檔案選擇介面
    }

    override fun onJsAlert(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        val alertDialog: AlertDialog = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(R.string.confirm2, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    result.confirm()
                }
            })
            .setCancelable(false)
            .create()
        alertDialog.show()
        return true
    }

    override fun onJsConfirm(
        view: WebView?,
        url: String?,
        message: String?,
        result: JsResult
    ): Boolean {
        val alertDialog: AlertDialog = AlertDialog.Builder(context)
            .setMessage(message)
            .setPositiveButton(R.string.confirm2, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    result.confirm()
                }
            })
            .setNegativeButton(R.string.cancel, object : DialogInterface.OnClickListener {
                override fun onClick(dialogInterface: DialogInterface?, i: Int) {
                    result.cancel()
                }
            })
            .setCancelable(false)
            .create()
        alertDialog.show()
        return true
    }

    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
//            Log.i(TAG +"  console" , "["+consoleMessage.messageLevel()+"] "+ consoleMessage.message());
        return true
    }
}