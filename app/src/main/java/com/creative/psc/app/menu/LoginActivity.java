package com.creative.psc.app.menu;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.creative.psc.app.R;
import com.creative.psc.app.fragment.FragMenuActivity;
import com.creative.psc.app.retrofit.Datas;
import com.creative.psc.app.retrofit.LoginDatas;
import com.creative.psc.app.retrofit.RetrofitService;
import com.creative.psc.app.util.BackPressCloseSystem;
import com.creative.psc.app.util.SettingPreference;
import com.creative.psc.app.util.UtilClass;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";
    private ProgressDialog pDlalog = null;
    private String android_id;
    private boolean valid = true;

    private BackPressCloseSystem backPressCloseSystem;

    @Bind(R.id.editText1) EditText _sabun_no;
    @Bind(R.id.editText2) EditText _user_sosok;
    @Bind(R.id.editText3) EditText _user_nm;
    @Bind(R.id.editText4) EditText _user_pw;
    @Bind(R.id.button1) Button loginButton;

    private String j_pos;
    private String part1_cd;
    private String part2_cd;
    private boolean getUserCheck= false;

    SettingPreference pref = new SettingPreference("loginData",this);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);
        ButterKnife.bind(this);
        android_id = "" + android.provider.Settings.Secure.getString(getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        backPressCloseSystem = new BackPressCloseSystem(this);
        loadLoginData();

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

    }//onCreate

    @Override
    protected void onNewIntent(Intent intent) {

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void saveLoginData(Response<LoginDatas> response) {
        String sabun_noStr= _sabun_no.getText().toString();
        String user_nm= _user_nm.getText().toString();
        String user_sosok= _user_sosok.getText().toString();
        String user_pwStr= _user_pw.getText().toString();
        String latest_app_ver="";
        try {
            latest_app_ver= response.body().getLATEST_APP_VER();

        } catch (Exception e) {
            e.printStackTrace();
        }

        pref.put("sabun_no",sabun_noStr);
        pref.put("user_nm",user_nm);
        pref.put("user_sosok",user_sosok);
        pref.put("user_pw",user_pwStr);
        pref.put("j_pos",j_pos);
        pref.put("part1_cd",part1_cd);
        pref.put("part2_cd",part2_cd);
        pref.put("LATEST_APP_VER",latest_app_ver);
    }

    private void loadLoginData() {
        String sabun_no= pref.getValue("sabun_no","");
        String user_nm= pref.getValue("user_nm","");
        String user_sosok= pref.getValue("user_sosok","");
        String user_pw= pref.getValue("user_pw","");
        j_pos= pref.getValue("j_pos","");
        part1_cd= pref.getValue("part1_cd","");
        part2_cd= pref.getValue("part2_cd","");

        _sabun_no.setText(sabun_no);
        _user_nm.setText(user_nm);
        _user_sosok.setText(user_sosok);
        _user_pw.setText(user_pw);

        if(sabun_no.length()>0){
            getUserInfo();
        }

/*        Toast.makeText(getApplicationContext(), "sabun_no : " + sabun_no+",user_nm : " + user_nm+",user_sosok : "
                + user_sosok+",user_pw : " + user_pw, Toast.LENGTH_LONG).show();*/
    }

    public void getUserInfo(){
        InputMethodManager imm= (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(_sabun_no.getWindowToken(), 0);

        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(LoginActivity.this);
        UtilClass.showProcessingDialog(pDlalog);
        loginButton.setEnabled(false);

        Call<Datas> call = service.listData("Login", "sid_check_ajax", "sid="+_sabun_no.getText());
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    if(status.equals("true")){
                        getUserCheck= true;
                        _user_sosok.setText(response.body().getList().get(0).get("buseo_nm").toString());
                        _user_nm.setText(response.body().getList().get(0).get("user_nm").toString().trim());
                        _user_sosok.setError(null);
                        _user_nm.setError(null);
                    }else{
                        Toast.makeText(getApplicationContext(), "데이터가 없습니다.",Toast.LENGTH_SHORT).show();
                        _user_sosok.setText("");
                        _user_nm.setText("");
                    }

                }else{
                    Toast.makeText(getApplicationContext(), "사용자 조회에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
                loginButton.setEnabled(true);
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                loginButton.setEnabled(true);
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure UserInfo",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void loginCheck(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);
        Map<String, Object> map = new HashMap();
        map.put("sabun_no",_sabun_no.getText());
        map.put("password",_user_pw.getText());

        final ProgressDialog dialog = new ProgressDialog(LoginActivity.this);
        UtilClass.showProcessingDialog(dialog);
        loginButton.setEnabled(false);

        Call<LoginDatas> call = service.loginData(map);
        call.enqueue(new Callback<LoginDatas>() {
            @Override
            public void onResponse(Call<LoginDatas> call, Response<LoginDatas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    if(response.body().getResult()==1) {
                        try {
                            onLoginSuccess();
                            saveLoginData(response);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"에러코드  Login 1",Toast.LENGTH_SHORT).show();
                        }
                    }else if( response.body().getResult()==2) {
                        try {
                            onLoginFailed();

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"에러코드  Login 2",Toast.LENGTH_SHORT).show();
                        }
                    }else if( response.body().getResult()==3) {
                        try {
                            int flag= response.body().getFlag();
                            onLoginFailed3(flag);

                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(),"에러코드  Login 3",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d(TAG,"Data is Null");
                        onLoginFailed2();
                    }
                }else{
                    Toast.makeText(getApplicationContext(), "로그인에 실패하였습니다.",Toast.LENGTH_SHORT).show();
                    loginButton.setEnabled(true);
                }
                if(dialog!=null) dialog.dismiss();
            }

            @Override
            public void onFailure(Call<LoginDatas> call, Throwable t) {
                if(dialog!=null) dialog.dismiss();
                loginButton.setEnabled(true);
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getApplicationContext(), "onFailure Login",Toast.LENGTH_SHORT).show();
            }
        });
    }

    @OnClick(R.id.editText1)  //사번 바꿨는지
    public void sabunNoCheck() {
        getUserCheck= false;
    }

    @OnClick(R.id.button3)  //사용자 정보 조회
    public void getUserInfoMenu() {
        getUserInfo();
    }


    public void login() {
        Log.d(TAG, "Login");

        if (!validate()) {
            onLoginFailed();
            return;
        }
        UtilClass.logD(TAG, "check="+getUserCheck);
        if (!getUserCheck) {  //유저 정보 조회 했는지
            Toast.makeText(getBaseContext(), "정보 조회를 해주세요.", Toast.LENGTH_LONG).show();
            return;
        }

        //로그인 체크
        if(valid) loginCheck();
    }

    @Override
    public void onBackPressed() {
        backPressCloseSystem.onBackPressed();
    }

    public void onLoginSuccess() {
        loginButton.setEnabled(true);
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.putExtra("title", "메인");
        intent.putExtra("mode", "login");
        startActivity(intent);
    }

    public void onLoginFailed() {
        Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n아이디 또는 비밀번호를 확인해 주세요.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public void onLoginFailed2() {
        Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n서버 정보를 확인해 주세요.", Toast.LENGTH_LONG).show();
        loginButton.setEnabled(true);
    }

    public void onLoginFailed3(int flag) {
        if(flag==1){
            Toast.makeText(getBaseContext(), "접속에 실패 하였습니다.\n아이디 또는 비밀번호를 확인해 주세요.", Toast.LENGTH_LONG).show();
        }else if(flag==2){
            Toast.makeText(getBaseContext(), "해당아이디는 사용권한이 없습니다.\n관리자에게 문의 바랍니다.", Toast.LENGTH_LONG).show();
        }else if(flag==3){
            Toast.makeText(getBaseContext(), "해당업체는 사용권한이 없습니다.\n관리자에게 문의 바랍니다.", Toast.LENGTH_LONG).show();
        }else if(flag==4){
            Toast.makeText(getBaseContext(), "사용기간이 만료되었습니다.\n관리자에게 문의 바랍니다.", Toast.LENGTH_LONG).show();
        }else if(flag==5){
            Toast.makeText(getBaseContext(), "현재기기에 등록된 아이디가 아닙니다.\n관리자에게 문의 바랍니다.", Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(getBaseContext(), "현재 아이디가 다른기기에 등록되있습니다.\n관리자에게 문의 바랍니다.", Toast.LENGTH_LONG).show();
        }
        loginButton.setEnabled(true);
    }

    public boolean validate() {
        valid = true;
        String user_id = _user_nm.getText().toString();
        String password = _user_pw.getText().toString();

        if (user_id.isEmpty()) {
            _user_nm.setError("이름을 입력하세요.");
            valid = false;
        } else {
            _user_nm.setError(null);
        }

        if (password.isEmpty() || password.length() <= 3 || password.length() >= 16) {
            _user_pw.setError("비밀번호를 4자리이상 15자리이하로 입력하세요.");
            valid = false;
        } else {
            _user_pw.setError(null);
        }

        return valid;
    }

}
