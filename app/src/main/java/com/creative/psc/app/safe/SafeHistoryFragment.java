package com.creative.psc.app.safe;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JavascriptInterface;
import android.webkit.JsResult;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.creative.psc.app.R;
import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SafeHistoryFragment extends Fragment {

    private static final String TAG = "SafeMainFragment";
    private static String UPLOAD_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/fileUpload.do";
    private String url;
    private String sabun_no;
    private String selectDaeClassKey;
    private String selectJungClassKey;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.webView1) WebView webView;
    @Bind(R.id.textView1) TextView bottomText1;
    @Bind(R.id.textView2) TextView bottomText2;
    private ProgressDialog dialog;

    private PermissionListener permissionlistener;

    public SafeHistoryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.safe_main, container, false);
        ButterKnife.bind(this, view);
        dialog = new ProgressDialog(getActivity());

        sabun_no = MainFragment.loginSabun;
        url= getArguments().getString("url");
        selectDaeClassKey= getArguments().getString("selectDaeClassKey");
        selectJungClassKey= getArguments().getString("selectJungClassKey");

        textTitle.setText("안전관리이력");
        view.findViewById(R.id.layout_bottom).setVisibility(View.GONE);

        final Context myApp = getActivity();
        //자바스크립트 Alert,confirm 사용
        webView.setWebChromeClient(new WebChromeClient() {
            ProgressBar pb = (ProgressBar)view.findViewById(R.id.progressBar1);

            @Override
            public boolean onJsAlert(WebView view, String url, String message, final JsResult result) {
                new AlertDialog.Builder(myApp)
                        .setTitle("경고")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok,
                                new AlertDialog.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }
            @Override
            public boolean onJsConfirm(WebView view, String url, String message, final JsResult result) {
                // TODO Auto-generated method stub
                //return super.onJsConfirm(view, url, message, result);
                new AlertDialog.Builder(view.getContext())
                        .setTitle("알림")
                        .setMessage(message)
                        .setPositiveButton("네",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.confirm();
                                    }
                                })
                        .setNegativeButton("아니오",
                                new AlertDialog.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        result.cancel();
                                    }
                                })
                        .setCancelable(false)
                        .create()
                        .show();
                return true;
            }

            public void onProgressChanged(WebView webView, int paramInt) {
                this.pb.setProgress(paramInt);
                if (paramInt == 100)
                {
                    this.pb.setVisibility(View.GONE);
                    return;
                }
                this.pb.setVisibility(View.VISIBLE);
            }
        });//setWebChromeClient 재정의

        WebSettings wSetting = webView.getSettings();
        webView.setWebViewClient(new WebViewClient()); // 이걸 안해주면 새창이 뜸
        webView.setWebViewClient(new MyWebViewClient());
        wSetting.setJavaScriptEnabled(true);      // 웹뷰에서 자바 스크립트 사용
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);

        webView.addJavascriptInterface(new AndroidBridge(), "android");
        webView.loadUrl(url);

        return view;
    }

    private class AndroidBridge {
        @JavascriptInterface
        public void modifyItem(final String paramString,final String paramString2,final String paramString3){
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Fragment fragment = new DangerMainFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("selectDaeClassKey",paramString);
                    bundle.putString("selectJungClassKey",paramString2);
                    bundle.putString("idx",paramString3);
                    bundle.putString("mode","update");
                    bundle.putString("url",MainFragment.ipAddress+ MainFragment.contextPath+"/Safe/safe_writeModify.do?large_cd="
                            +paramString+"&mid_cd="+paramString2+"&idx="+ paramString3+"&sabun_no="+ sabun_no);

                    onFragment(fragment,bundle,"안전관리작성");

                }
            });
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url){
//            Log.d("shouldOveride","웹뷰클릭 됨="+url);
            view.loadUrl(url);

            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            UtilClass.showProgressDialog(dialog);
        }
        @Override
        public void onPageFinished(WebView view, String url) {
            webView.clearHistory();
            webView.clearCache(true);
            webView.clearView();
            super.onPageFinished(view, url);
            try {
                UtilClass.closeProgressDialog(dialog);
            } catch (Exception e) {
                // TODO: handle exception
            }

        }

        @Override
        public void onReceivedError(WebView view, int errorCode,String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            Log.d("onReceivedError", "errorCode=" + errorCode);
            switch(errorCode) {
                case ERROR_AUTHENTICATION:              // 서버에서 사용자 인증 실패
                case ERROR_BAD_URL:                     // 잘못된 URL
                case ERROR_CONNECT:                     // 서버로 연결 실패
                case ERROR_FAILED_SSL_HANDSHAKE:     	// SSL handshake 수행 실패
                case ERROR_FILE:                        // 일반 파일 오류
                case ERROR_FILE_NOT_FOUND:              // 파일을 찾을 수 없습니다
                case ERROR_HOST_LOOKUP:            		// 서버 또는 프록시 호스트 이름 조회 실패
                case ERROR_IO:                          // 서버에서 읽거나 서버로 쓰기 실패
                case ERROR_PROXY_AUTHENTICATION:    	// 프록시에서 사용자 인증 실패
                case ERROR_REDIRECT_LOOP:               // 너무 많은 리디렉션
                case ERROR_TIMEOUT:                     // 연결 시간 초과
                case ERROR_TOO_MANY_REQUESTS:           // 페이지 로드중 너무 많은 요청 발생
                case ERROR_UNKNOWN:                     // 일반 오류
                case ERROR_UNSUPPORTED_AUTH_SCHEME:  	// 지원되지 않는 인증 체계
                case ERROR_UNSUPPORTED_SCHEME:			// URI가 지원되지 않는 방식

                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                    builder.setTitle("Error");
                    builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
//                            Fragment fm = getFragmentManager().findFragmentByTag("dfdf");
                            getActivity().finish();
                        }
                    });
                    builder.setMessage("네트워크 상태가 원활하지 않습니다. 잠시 후 다시 시도해 주세요.");
                    builder.show();

                    break;
            }
        }
    }//MyWebViewClient

    public void onFragment(Fragment fragment, Bundle bundle, String title){
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentReplace, fragment);
        fragmentTransaction.addToBackStack(title);

        bundle.putString("title", title);
        fragment.setArguments(bundle);
        fragmentTransaction.commit();
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

}
