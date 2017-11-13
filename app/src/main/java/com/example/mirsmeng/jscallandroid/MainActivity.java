package com.example.mirsmeng.jscallandroid;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import static android.R.attr.value;
import static android.R.id.message;


/**
 * http://blog.csdn.net/carson_ho/article/details/64904691
 *
 * 对于Android调用JS代码的方法有2种：
 1. 通过WebView的loadUrl（）
 2. 通过WebView的evaluateJavascript（）//4.4(含4.4)以上可以用  效率高

 对于JS调用Android代码的方法有3种：
 1. 通过WebView的addJavascriptInterface（）进行对象映射

 JS通过WebView调用 Android 代码 方式一
 webView.addJavascriptInterface(new AndroidtoJs(),"test");
 //   步骤1：定义一个与JS对象映射关系的Android类：AndroidtoJs
 public class AndroidtoJs extends  Object{

        @JavascriptInterface
        public void hello(String str){
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        }
}

 2. 通过 WebViewClient 的shouldOverrideUrlLoading ()方法回调拦截 url




 3. 通过 WebChromeClient 的onJsAlert()、onJsConfirm()、onJsPrompt（）方法回调拦截JS对话框alert()、confirm()、prompt（） 消息
 *
 */

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private WebView webView;
    private Button btn;
    Map<String,String> map = new LinkedHashMap<String, String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = (WebView) findViewById(R.id.webview);
//        webView.setWebChromeClient(new WebChromeClient());
        webView.loadUrl("file:///android_asset/js.html");

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);

        btn = (Button) findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        //安卓调用js方法
                        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.KITKAT){//安卓4.4以上使用这个  效率高
                            webView.evaluateJavascript("javascript:callJS()", new ValueCallback<String>() {
//                            webView.evaluateJavascript("javascript:callAndroid3("+2222+")", new ValueCallback<String>() {
                                @Override
                                public void onReceiveValue(String value) {
                                }
                            });
                        }else{//4.4以下使用这个
                            webView.loadUrl("javascript:callJS()");
                        }
                    }
                });
            }
        });

//        //可以修改标题  按钮文字
        webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
//                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
//                adb.setTitle("警告框");
//                adb.setMessage(message);
//                adb.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        result.confirm();
//                    }
//                });
//                adb.setCancelable(false);
//                adb.create().show();

                AlertDialog.Builder adb = new AlertDialog.Builder(MainActivity.this);
                adb.setTitle("警告框");
                adb.setMessage(message);
                adb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.confirm();
                    }
                });
                adb.setNegativeButton("否", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        result.cancel();
                    }
                });
                adb.setCancelable(false);
                adb.create().show();

                return true;
            }
        });


//        JS通过WebView调用 Android 代码 方式一
        webView.addJavascriptInterface(new AndroidtoJs(),"test");


//        JS通过WebView调用 Android 代码 方式二  和前端约定好数据格式  /*约定的url协议为：js://webview?arg1=111&arg2=222*/
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Uri uri = Uri.parse(url);

                if(uri.getScheme().equals("js")){
                    if(uri.getAuthority().equals("webview")){

                        Set<String> queryParameterNames = uri.getQueryParameterNames();
                        for (String pn : queryParameterNames) {
                            map.put(pn,uri.getQueryParameter(pn));
                        }
                        Log.d(TAG, "map:"+map);
                        return true;
                    }

                }

                return super.shouldOverrideUrlLoading(view, url);
            }
        });

    }
//    步骤1：定义一个与JS对象映射关系的Android类：AndroidtoJs
    public class AndroidtoJs extends  Object{

        @JavascriptInterface
        public void hello(String str){
            Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
        }
    }

}
