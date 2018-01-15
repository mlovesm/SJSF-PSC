package com.creative.psc.app.gear;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.psc.app.R;
import com.creative.psc.app.adaptor.BaseAdapter;
import com.creative.psc.app.fragment.FragMenuActivity;
import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.util.KeyValueArrayAdapter;
import com.creative.psc.app.util.SettingPreference;
import com.creative.psc.app.util.UtilClass;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class GearPopupActivity extends Activity {
    private static final String TAG = "GearPopupActivity";
    private String l_url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Gear/gearGubunList";
    private String m_url = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Gear/gearInfoList/gear_cd=";
    private String driverUrl = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Gear/gearDriverList";

    private String[] daeClassKeyList;
    private String[] daeClassValueList;
    private String[] jungClassKeyList;
    private String[] jungClassValueList;
    private String[] driverNameList;
    String selectDaeClassKey="";
    String selectJungClassKey="";
    String selectDriver="";

    @Bind(R.id.spinner1) Spinner spn_daeClass;
    @Bind(R.id.spinner2) Spinner spn_jungClass;
    @Bind(R.id.textView1) TextView _driver_name;

    //검색 다이얼로그
    private Dialog mDialog = null;
    private Spinner search_spi;
    private String search_gubun;	//검색 구분
    private EditText et_search;
    private ListView listView;
    private ArrayList<HashMap<String,String>> arrayList;
    private BaseAdapter mAdapter;
    private Button btn_search;
    private TextView btn_cancel;

    private SettingPreference pref = new SettingPreference("loginData",this);
    private AQuery aq = new AQuery( this );

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.gear_popup);
        ButterKnife.bind(this);
        this.setFinishOnTouchOutside(false);

        getDaeClassData();
        loadLoginData();

        spn_daeClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectDaeClassKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));

                async_progress_dialog("getJungClass");

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        spn_jungClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                KeyValueArrayAdapter adapter = (KeyValueArrayAdapter) parent.getAdapter();
                selectJungClassKey= adapter.getEntryValue(position);
                UtilClass.logD("LOG", "KEY : " + adapter.getEntryValue(position));
                UtilClass.logD("LOG", "VALUE : " + adapter.getEntry(position));

                String driverName= driverNameList[position];
                _driver_name.setText(driverName);

            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

    }//onCreate

    private void loadLoginData() {
        String sabun_no= pref.getValue("sabun_no","");
        String user_nm= pref.getValue("user_nm","");
        String user_sosok= pref.getValue("user_sosok","");
        String user_pw= pref.getValue("user_pw","");

    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(GearPopupActivity.this, "", "Loading...", true);

        dialog.setIndeterminate(true);
        dialog.setCancelable(true);
        dialog.setInverseBackgroundForced(false);
        dialog.setCanceledOnTouchOutside(true);

        String url="";
        if(callback.equals("getJungClass")){
            url= m_url+selectDaeClassKey;

        }else if(callback.equals("getDriverList")){
            if(!et_search.getText().toString().equals("")){
                UtilClass.logD(TAG, "검색있음");
                url= driverUrl +"/search="+search_gubun+"/keyword="+et_search.getText();
            }else{
                UtilClass.logD(TAG, "검색X");
                url= driverUrl;

            }

        }
        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getDaeClassData() {
        aq.ajax( l_url, JSONObject.class, new AjaxCallback<JSONObject>() {
            @Override
            public void callback(String url, JSONObject object, AjaxStatus status ) {
                if( object != null) {
                    try {
                        daeClassKeyList= new String[object.getJSONArray("datas").length()+1];
                        daeClassValueList= new String[object.getJSONArray("datas").length()+1];

                        daeClassKeyList[0]= "0";
                        daeClassValueList[0]= "선택하세요";
                        for(int i=0; i<object.getJSONArray("datas").length();i++){
                            daeClassKeyList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("gear_gcd").toString();
                            daeClassValueList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("gear_gnm").toString();
                        }

                        KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(GearPopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        adapter.setEntries(daeClassValueList);
                        adapter.setEntryValues(daeClassKeyList);

                        spn_daeClass.setPrompt("차종");
                        spn_daeClass.setAdapter(adapter);
                    } catch ( Exception e ) {
                        Toast.makeText(getApplicationContext(),"에러코드 Gear 1", Toast.LENGTH_SHORT).show();
                    }
                }else{
                    UtilClass.logD(TAG,"Data is Null");
                    Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        } );
    }

    public void getJungClass(String url, JSONObject object, AjaxStatus status) throws JSONException {
        if( object != null) {
            try {
                jungClassKeyList= new String[object.getJSONArray("datas").length()+1];
                jungClassValueList= new String[object.getJSONArray("datas").length()+1];

                jungClassKeyList[0]= "0";
                jungClassValueList[0]= "선택하세요";

                driverNameList= new String[object.getJSONArray("datas").length()+1];
                driverNameList[0]= "";
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    jungClassKeyList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("gear_cd").toString().trim();
                    jungClassValueList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("gear_nm").toString()
                            +"   "+object.getJSONArray("datas").getJSONObject(i).get("gear_cd").toString().trim();
                    if(!object.getJSONArray("datas").getJSONObject(i).get("driver_nm").toString().trim().equals("")){
                        jungClassValueList[i+1]+= "   ["+object.getJSONArray("datas").getJSONObject(i).get("driver_nm").toString().trim()+"]";
                    }
                    driverNameList[i+1]= object.getJSONArray("datas").getJSONObject(i).get("driver_nm").toString();
                }

                KeyValueArrayAdapter adapter = new KeyValueArrayAdapter(GearPopupActivity.this, android.R.layout.simple_spinner_dropdown_item);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                adapter.setEntries(jungClassValueList);
                adapter.setEntryValues(jungClassKeyList);

                spn_jungClass.setPrompt("장비번호");
                spn_jungClass.setAdapter(adapter);
            } catch ( Exception e ) {
                Toast.makeText(getApplicationContext(),"에러코드 Gear 2", Toast.LENGTH_SHORT).show();
            }
        }else{
            UtilClass.logD(TAG,"Data is Null");
            Toast.makeText(getApplicationContext(),"데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //다이얼로그
    private void driverSearchDialog() {
        final View linear = View.inflate(getApplicationContext(), R.layout.search_dialog_list, null);
        mDialog = new Dialog(GearPopupActivity.this);
        mDialog.setTitle("검색");

        search_spi= (Spinner) linear.findViewById(R.id.search_spi);
        et_search= (EditText) linear.findViewById(R.id.et_search);
        listView= (ListView) linear.findViewById(R.id.listView1);

        ArrayAdapter adapter = ArrayAdapter.createFromResource(getApplicationContext(), R.array.user_list, R.layout.spinner_item2);
        		adapter.setDropDownViewResource(R.layout.spinner_dropdown_item);

        search_spi.setPrompt("선택하세요.");
        search_spi.setAdapter(adapter);
        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
//				search_spi.getSelectedItem().toString();
                if(position==0){
                    search_gubun="user_nm";
                }else if(position==1){
                    search_gubun="user_no";
                }else{
                    search_gubun=null;
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mDialog.setContentView(linear);

        WindowManager.LayoutParams params= mDialog.getWindow().getAttributes();
        mDialog.getWindow().setAttributes(params);

        // Back키 눌렀을 경우 Dialog Cancle 여부 설정
        mDialog.setCancelable(true);

        // Dialog 생성시 배경화면 어둡게 하지 않기
//		mDialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // Dialog 밖을 터치 했을 경우 Dialog 사라지게 하기
        mDialog.setCanceledOnTouchOutside(true);

        btn_search = (Button) linear.findViewById(R.id.button1);
        btn_cancel = (TextView) linear.findViewById(R.id.textButton1);

        btn_search.setOnClickListener(button_click_listener);
        btn_cancel.setOnClickListener(button_click_listener);
        listView.setOnItemClickListener(new ListViewItemClickListener());

        // Dialog Cancle시 Event 받기
        mDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
                dismissDialog();
            }
        });

        // Dialog Show시 Event 받기
        mDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {

            }
        });

        // Dialog Dismiss시 Event 받기
        mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if(mDialog != null && mDialog.isShowing()) {
                    mDialog.dismiss();
                }
            }
        });

        mDialog.show();

        async_progress_dialog("getDriverList");
    }

    private void dismissDialog() {
        if(mDialog != null && mDialog.isShowing()) {
            mDialog.dismiss();
        }
    }

    public void getDriverList(String url, JSONObject object, AjaxStatus status) {
        if( object != null) {
            try {
                arrayList = new ArrayList<>();
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,String> hashMap = new HashMap<>();

                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("sabun").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("user_nm").toString());

                    arrayList.add(hashMap);
                }

                mAdapter = new BaseAdapter(getApplicationContext(), arrayList);
                listView.setAdapter(mAdapter);
            } catch ( Exception e ) {
                Toast.makeText(getApplicationContext(), "에러코드 Gear 3", Toast.LENGTH_SHORT).show();
            }
        }else{
            Log.d(TAG,"Data is Null");
            Toast.makeText(getApplicationContext(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener button_click_listener = new View.OnClickListener() {

        public void onClick(View v) {
            switch (v.getId()){
                case R.id.button1:
                    //검색하면 키보드 내리기
                    InputMethodManager imm= (InputMethodManager)getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
                    async_progress_dialog("getDriverList");
                    break;

                case R.id.textButton1:
                    dismissDialog();
                    break;
            }
        }
    };

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            HashMap<String,String> hashMap = new HashMap<>();
//            hashMap = arrayList.get(position);
//            ArrayList<String> arr = new ArrayList<>();
//            for (Iterator iter = hashMap.entrySet().iterator(); iter.hasNext();) {
//                Map.Entry entry = (Map.Entry) iter.next();
//                //String key = (String)entry.getKey();
//                arr.add((String) entry.getValue());
//            }
//            Log.d(TAG, "?="+arr);
            selectDriver= arrayList.get(position).get("data1").toString();
            _driver_name.setText(arrayList.get(position).get("data2").toString());

            InputMethodManager imm= (InputMethodManager)getApplication().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);
            dismissDialog();
        }
    }

    @OnClick(R.id.textView4)
    public void driverOpenPopup() {
        driverSearchDialog();
    }

    @OnClick(R.id.textView3)
    public void closePopup() {
        finish();
    }


    @OnClick(R.id.textView2)   //작성
    public void nextDataInfo() {
        if(selectDaeClassKey.equals("0")||selectJungClassKey.equals("0")){
            Toast.makeText(getApplicationContext(), "항목을 선택하세요.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(getBaseContext(),FragMenuActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("title", "장비점검");
        intent.putExtra("selectGearKey", selectJungClassKey);
        intent.putExtra("selectDriver", selectDriver);
        startActivity(intent);
        finish();
    }
}
