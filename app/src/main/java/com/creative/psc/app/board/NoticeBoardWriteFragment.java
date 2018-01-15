package com.creative.psc.app.board;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.psc.app.R;
import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.retrofit.Datas;
import com.creative.psc.app.retrofit.RetrofitService;
import com.creative.psc.app.util.KeyValueArrayAdapter;
import com.creative.psc.app.util.UtilClass;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NoticeBoardWriteFragment extends Fragment {
    private static final String TAG = "NoticeBoardWriteFragment";
    private ProgressDialog pDlalog = null;
    private RetrofitService service;

    private String mode="";
    private String idx="";
    private String dataSabun;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.editText1) EditText et_title;
    @Bind(R.id.editText2) EditText et_memo;
    @Bind(R.id.spinner1) Spinner spn;
    @Bind(R.id.radioGroup) RadioGroup radioGroup;
    @Bind(R.id.textView1) TextView tv_writerName;

    private String selectedPostionKey;  //스피너 선택된 키값
    private int selectedPostion=0;    //스피너 선택된 Row 값
    private String selectedRadio="A";    //라디오 선택된 라디오 값
    private String push_target;

    private String url = MainFragment.ipAddress+MainFragment.contextPath+"/rest/Board/noticeBoardCodeList/push_target/";
    private String selectGubunKey="";
    private String[] gubunKeyList;
    private String[] gubunValueList;

    private AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.basic_write, container, false);
        ButterKnife.bind(this, view);
        service= RetrofitService.rest_api.create(RetrofitService.class);
        aq = new AQuery( getActivity() );

        mode= getArguments().getString("mode");
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        if(mode.equals("insert")){
            dataSabun= MainFragment.loginSabun;
            view.findViewById(R.id.linear3).setVisibility(View.GONE);
            textTitle.setText("공지사항 작성");
            tv_writerName.setText(MainFragment.loginName);
            radioGroup.check(R.id.radio1);
            getNoticeBoardCodeData("A");
        }else{
            textTitle.setText("공지사항 수정");
            idx= getArguments().getString("push_key");
            push_target= getArguments().getString("push_target");
            UtilClass.logD(TAG, "push_target="+push_target);
            if(push_target.equals("A")) {
                radioGroup.check(R.id.radio1);
            }else if(push_target.equals("F")) {
                selectedRadio="F";
                radioGroup.check(R.id.radio2);
            }else{
                selectedRadio="J";
                radioGroup.check(R.id.radio3);
            }
            async_progress_dialog();
        }

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId){
                    case R.id.radio1:
                        selectGubunKey="";
                        selectedPostion=0;
                        selectedRadio="A";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    case R.id.radio2:
                        selectedPostion=0;
                        selectedRadio="F";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    case R.id.radio3:
                        selectedPostion=0;
                        selectedRadio="J";
                        getNoticeBoardCodeData(selectedRadio);
                        break;
                    default:
                        break;
                }
            }
        });

        spn.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectGubunKey= adapter.getEntryValue(position);

                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }//onCreateView

    public void async_progress_dialog(){
        RetrofitService service = RetrofitService.rest_api.create(RetrofitService.class);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.listData("Board","noticeBoardDetail", idx);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        dataSabun= response.body().getList().get(0).get("input_id").toString();
                        if(MainFragment.loginSabun.equals(dataSabun)){
                        }else{
                            et_title.setFocusableInTouchMode(false);
                            et_memo.setFocusableInTouchMode(false);
                            getActivity().findViewById(R.id.linear1).setVisibility(View.GONE);
                            getActivity().findViewById(R.id.linear2).setVisibility(View.GONE);
                            getActivity().findViewById(R.id.linear3).setVisibility(View.GONE);
                        }
                        selectedPostionKey = response.body().getList().get(0).get("detail_target").toString();
                        et_title.setText(response.body().getList().get(0).get("push_title").toString());
                        et_memo.setText(response.body().getList().get(0).get("push_text").toString());
                        tv_writerName.setText(response.body().getList().get(0).get("input_nm").toString());

                        getNoticeBoardCodeData(selectedRadio);

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 NoticeBoard 2", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure Board",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void getNoticeBoardCodeData(String push_target) {
        aq.ajax( url+push_target, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        gubunKeyList= new String[object.getJSONArray("datas").length()];
                        gubunValueList= new String[object.getJSONArray("datas").length()];
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            gubunKeyList[i]= object.getJSONArray("datas").getJSONObject(i).get("code").toString();
                            if(gubunKeyList[i].equals(selectedPostionKey))  selectedPostion= i;
                            gubunValueList[i]= object.getJSONArray("datas").getJSONObject(i).get("name").toString();
                        }
                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntryValues(gubunKeyList);
                        adapter.setEntries(gubunValueList);

                        spn.setPrompt("구분");
                        spn.setAdapter(adapter);
                        spn.setSelection(selectedPostion);
                    } catch ( Exception e ) {

                    }
                }else{
                    UtilClass.logD(TAG,"getNoticeBoardCodeData Data is Null");
                    Toast.makeText(getActivity(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );

    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @OnClick({R.id.textView4})
    public void alertDialogPush(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("P");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textView2, R.id.top_save})
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("S");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textView3})
    public void alertDialogDelete(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("D");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    public void alertDialog(final String gubun){
        final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("알림");
        if(gubun.equals("S")){
            alertDlg.setMessage("작성하시겠습니까?");
        }else if(gubun.equals("D")){
            alertDlg.setMessage("삭제하시겠습니까?");
        }else{
            alertDlg.setMessage("전송하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else if(gubun.equals("D")){
                    deleteData();
                }else{
                    onPushData();
                }
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();  // AlertDialog를 닫는다.
            }
        });
        alertDlg.show();
    }

    //삭제
    public void deleteData() {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.deleteData("Board","noticeBoardDelete", idx);

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse NoticeBoard",Toast.LENGTH_LONG).show();
            }
        });
    }

    //작성,수정
    public void postData() {
        String push_title = et_title.getText().toString();
        String push_text = et_memo.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap();
        map.put("writer_sabun", MainFragment.loginSabun);
        map.put("writer_name", MainFragment.loginName);
        map.put("push_title",push_title);
        map.put("push_text",push_text);
        map.put("push_target",selectedRadio);
        map.put("detail_target",selectGubunKey);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call= null;
        if(mode.equals("insert")){
            call = service.insertData("Board","noticeBoardWrite", map);
        }else{
            call = service.updateData("Board","noticeBoardModify", map);
            map.put("idx",idx);
        }

        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse NoticeBoard",Toast.LENGTH_LONG).show();
            }
        });

    }

    //푸시 전송
    public void onPushData() {
        String push_title = et_title.getText().toString();
        String push_text = et_memo.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }
        Map<String, Object> map = new HashMap();
        map.put("writer_sabun", MainFragment.loginSabun);
        map.put("writer_name", MainFragment.loginName);
        map.put("title", push_title);
        map.put("message", push_text);

        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.sendData("Board","sendPushData",map);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    handleResponse(response);
                }else{
                    Toast.makeText(getActivity(), "작업에 실패하였습니다.",Toast.LENGTH_LONG).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<Datas> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "handleResponse Push",Toast.LENGTH_LONG).show();
            }
        });

    }

    //작성 완료
    public void handleResponse(Response<Datas> response) {
        try {
            String status= response.body().getStatus();
            if(status.equals("success")){
                getActivity().onBackPressed();

            }else if(status.equals("successOnPush")){
                String pushSend= response.body().getList().get(0).get("pushSend").toString();

                if(pushSend.equals("success")){
                    Toast.makeText(getActivity(), "푸시데이터가 전송 되었습니다.",Toast.LENGTH_SHORT).show();
                }else if(pushSend.equals("empty")){
                    Toast.makeText(getActivity(), "푸시데이터를 받는 사용자가 없습니다.",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getActivity(), "푸시 전송이 실패하였습니다.",Toast.LENGTH_SHORT).show();
                }
            }else{
                Toast.makeText(getActivity(), "저장에 실패하였습니다.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "작업에 실패하였습니다.", Toast.LENGTH_SHORT).show();
        }

    }

}
