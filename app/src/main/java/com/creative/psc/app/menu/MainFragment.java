package com.creative.psc.app.menu;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.creative.psc.app.R;
import com.creative.psc.app.fragment.FragMenuActivity;
import com.creative.psc.app.retrofit.Datas;
import com.creative.psc.app.retrofit.RetrofitService;
import com.creative.psc.app.util.SettingPreference;
import com.creative.psc.app.util.UtilClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainFragment extends Fragment {

//    adb shell dumpsys activity activities | findstr "Run"
    private static final String TAG = "MainFragment";
    public static String ipAddress= "http://119.202.60.106:8585";
//    public static String ipAddress= "http://59.11.9.94:9090";
//    public static String ipAddress= "http://192.168.0.22:9191";
    public static String contextPath= "/sjsf_psc";
    private ProgressDialog pDlalog = null;

    //FCM 관련
    private String token=null;
    private String phone_num=null;
    public static boolean onAppCheck= false;
    public static String pendingPath= "";
    public static String pendingPathKey= "";
    private Activity mActivity;

    private PermissionListener permissionlistener;

    private SettingPreference pref;
    public static String loginSabun;
    public static String loginName;
    public static String jPos;
    public static String part1_cd;
    public static String part2_cd;
    public static String latestAppVer;

//    @Override public void onAttach(Context context) {
//        //This method avoid to call super.onAttach(context) if I'm not using api 23 or more
//        if (Build.VERSION.SDK_INT >= 23) {
//            super.onAttach(context);
//            onAttachToContext(context);
//        }
//    }
//
//    /*
//     * Deprecated on API 23
//     * Use onAttachToContext instead
//     */
//    @SuppressWarnings("deprecation")
//    @Override public void onAttach(Activity activity) {
//        super.onAttach(activity);
//        if (Build.VERSION.SDK_INT < 23) {
//            onAttachToContext(activity);
//        }
//    }

    protected void onAttachToContext(Context context) {
        this.mActivity = (Activity) context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.main, container, false);
        ButterKnife.bind(this, view);
        view.findViewById(R.id.top_home).setVisibility(View.GONE);
//        RelativeLayout linearLayout = (RelativeLayout) view.findViewById(R.id.top_line);
//        ViewGroup.LayoutParams param = new ViewGroup.LayoutParams
//                (ViewGroup.LayoutParams.MATCH_PARENT, 0);
//        linearLayout.setLayoutParams(param);
        pref = new SettingPreference("loginData",getActivity());

        loginSabun = pref.getValue("sabun_no","").trim();
        loginName = pref.getValue("user_nm","").trim();
        jPos = pref.getValue("j_pos","").trim();
        part1_cd = pref.getValue("part1_cd","");
        part2_cd = pref.getValue("part2_cd","");
        latestAppVer = pref.getValue("LATEST_APP_VER","");

        String currentAppVer= getAppVersion(getActivity());
        UtilClass.logD(TAG, "currentAppVer="+currentAppVer+", latestAppVer="+latestAppVer);

        token = FirebaseInstanceId.getInstance().getToken();
        UtilClass.logD(TAG, "Refreshed token: " + token);

        String mode= getArguments().getString("mode");
        if(mode.equals("first")){
            permissionlistener = new PermissionListener() {
                @Override
                public void onPermissionGranted() {
//                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
                    phone_num= getPhoneNumber();
                    postData();
                }

                @Override
                public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                    Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
                    phone_num="";
                    postData();
                }
            };
            new TedPermission(getActivity())
                    .setPermissionListener(permissionlistener)
                    .setRationaleMessage("전화번호 정보를 가져오기 위해선 권한이 필요합니다.")
                    .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                    .setGotoSettingButtonText("권한확인")
                    .setPermissions(Manifest.permission.CALL_PHONE)
                    .check();
        }
        onAppCheck= true;

        return view;
    }

    public static String getAppVersion(Context context) {
        // application version
        String versionName = "";
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionName = info.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            UtilClass.logD(TAG, "getAppVersion Error");
        }

        return versionName;
    }

    @OnClick(R.id.imageView1)
    public void getMenu1() {
        Intent intent = new Intent(getActivity(), FragMenuActivity.class);
        intent.putExtra("title", "공지사항");
        startActivity(intent);
    }

    @OnClick(R.id.imageView2)
    public void getMenu2() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "사람찾기");
        startActivity(intent);
    }

    @OnClick(R.id.imageView3)
    public void getMenu3() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "무재해현황판");
        startActivity(intent);
    }

    @OnClick(R.id.imageView4)
    public void getMenu4() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "작업기준");
        startActivity(intent);
    }

    @OnClick(R.id.imageView5)
    public void getMenu5() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "QSS 활동계획조회");
        startActivity(intent);
    }

    @OnClick(R.id.imageView6)
    public void getMenu6() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "연장근무현황");
        startActivity(intent);
//        Toast.makeText(getActivity(), "비활성화 메뉴 입니다.", Toast.LENGTH_SHORT);
    }

    @OnClick(R.id.imageView7)
    public void getMenu7() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "연차현황");
        startActivity(intent);
    }

    @OnClick(R.id.imageView8)
    public void getMenu8() {
        Intent intent = new Intent(getActivity(),FragMenuActivity.class);
        intent.putExtra("title", "급여명세서");
        startActivity(intent);
    }

    //푸시 데이터 전송
    public void postData() {
        String android_id = "" + android.provider.Settings.Secure.getString(getActivity().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);

        Map<String, Object> map = new HashMap();
        map.put("Token",token);
        map.put("phone_num",phone_num);
        map.put("sabun_no",loginSabun);
        map.put("and_id",android_id);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);
        Call<Datas> call = service.sendData("Board","fcmTokenData",map);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();

                }else{
                    Toast.makeText(getActivity(), "handleResponse Main",Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "토큰 생성에 실패하였습니다.",Toast.LENGTH_SHORT).show();
            }
        });

    }

    // 단말기 핸드폰번호 얻어오기
    public String getPhoneNumber() {
        String num = null;
        try {
            TelephonyManager tm = (TelephonyManager) getActivity().getSystemService(Context.TELEPHONY_SERVICE);
            num = tm.getLine1Number();
            if(num!=null&&num.startsWith("+82")){
                num = num.replace("+82", "0");
            }
        } catch (Exception e) {
            Toast.makeText(getActivity(), "오류가 발생 하였습니다!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return num;
    }
}
