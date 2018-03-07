package com.creative.psc.app.fcm;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.util.UtilClass;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

import java.io.IOException;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Created by GS on 2016-11-10.
 */
public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {
    private static final String TAG = "MyFirebaseIIDService";
//    private SettingPreference pref = new SettingPreference("loginData",this);
//    private String phone_num=null;

    // [START refresh_token]
    //처음 실행될때 자동으로 실행, 토큰 등록
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.
        String token = FirebaseInstanceId.getInstance().getToken();
        UtilClass.logD(TAG, "최초 token: " + token);

        // 생성등록된 토큰을 개인 앱서버에 보내 저장해 두었다가 추가 뭔가를 하고 싶으면 할 수 있도록 한다.
        //sendRegistrationToServer(token);
    }

    private void sendRegistrationToServer(String token) {

        // Add custom implementation, as needed.
        String android_id = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//        phone_num= getPhoneNumber();

        OkHttpClient client = new OkHttpClient();
        RequestBody body = new FormBody.Builder()
                .add("Token", token)
//                .add("phone_num", phone_num])
                .add("sub_data", android_id)
                .build();

        //request
        Request request = new Request.Builder()
                .url(MainFragment.ipAddress+MainFragment.contextPath+"/rest/Board/fcmTokenData")
                .post(body)
                .build();

        try {
            client.newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // 단말기 핸드폰번호 얻어오기
    public String getPhoneNumber() {
        String num=null;
        try{
            TelephonyManager tm = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            num= tm.getLine1Number();
        } catch(Exception e) {
            Toast.makeText(getApplicationContext(), "오류가 발생 하였습니다!", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        return num;
    }

}
