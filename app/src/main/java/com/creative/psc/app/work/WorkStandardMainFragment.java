package com.creative.psc.app.work;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxStatus;
import com.creative.psc.app.R;
import com.creative.psc.app.adaptor.BoardAdapter;
import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.retrofit.RetrofitService;
import com.creative.psc.app.util.FileDownProgressTask;
import com.creative.psc.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WorkStandardMainFragment extends Fragment {
    private static final String TAG = "WorkStandardMainFragment";
    private ProgressDialog pDlalog = null;
    private RetrofitService service;
    private PermissionListener permissionlistener;

    static final String TASK_FRAGMENT_TAG = "task";
    private static final int DIALOG_REQUEST_CODE = 1234;
    private String url;
    private String manual_kind;
    private String fileNm;
    private String fileSize;
    private String fileDir= Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Download" + File.separator;

    private ArrayList<HashMap<String,String>> arrayList;
    private BoardAdapter mAdapter;
    @Bind(R.id.listView1) ListView listView;
    @Bind(R.id.top_title) TextView textTitle;

    @Bind(R.id.search_top) LinearLayout layout;
    @Bind(R.id.search_spi) Spinner search_spi;
    @Bind(R.id.et_search) EditText et_search;
    String search_column;	//검색 컬럼

    private AQuery aq;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //화면 전환으로 MainFragment가 재생성되었을 경우를 고려해준다.
        //아직 동작 중일 TaskFragment를 태그로 찾아서
        TaskFragment taskFragment = (TaskFragment) getFragmentManager().findFragmentByTag(TASK_FRAGMENT_TAG);

        if (taskFragment != null) {//만약 TaskFragment가 동작 중이라면

            //setTargetFragment로 Target Fragment를 새로 생성된 MainFragment 인스턴스로 교체한다.
            //TaskFragment에서 getTargetFragment().onActivityResult를 호출하면 MainFragment의 onActivityResult가 호출되도록 설정된다.
            taskFragment.setTargetFragment(this, DIALOG_REQUEST_CODE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.search_basic_list, container, false);
        ButterKnife.bind(this, view);
        aq= new AQuery(getActivity());
        service= RetrofitService.rest_api.create(RetrofitService.class);

        view.findViewById(R.id.top_search).setVisibility(View.VISIBLE);
        layout.setVisibility(View.GONE);

        textTitle.setText(getArguments().getString("title"));
        manual_kind = getArguments().getString("code");

        async_progress_dialog("getBoardInfo");

        listView.setOnItemClickListener(new ListViewItemClickListener());
        // Spinner 생성
        ArrayAdapter adapter = ArrayAdapter.createFromResource(getActivity(), R.array.manual_list, android.R.layout.simple_spinner_dropdown_item);
//		search_spi.setPrompt("구분을 선택하세요.");
        search_spi.setAdapter(adapter);

        search_spi.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//				et_search.setText("position : " + position + parent.getItemAtPosition(position));
                et_search.setText("");
                et_search.setEnabled(true);
                if(position==0){
                    search_column="file_nm";
                }else if(position==1){
                    search_column="title";
                }else{

                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }//onCreateView

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, false);
        dialog.setInverseBackgroundForced(false);

        if(search_column!=null){
            url= MainFragment.ipAddress+MainFragment.contextPath+"/rest/Safe/workStandardList/"+manual_kind+"/search="+search_column+"/keyword="+et_search.getText();
        }else{
            url= MainFragment.ipAddress+MainFragment.contextPath+"/rest/Safe/workStandardList/"+manual_kind;
        }

        aq.progress(dialog).ajax(url, JSONObject.class, this, callback);
    }

    public void getBoardInfo(String url, JSONObject object, AjaxStatus status) throws JSONException {
        arrayList = new ArrayList<>();
        if(!object.get("count").equals(0)) {
            try {
                arrayList.clear();
                for(int i=0; i<object.getJSONArray("datas").length();i++){
                    HashMap<String,String> hashMap = new HashMap<>();
                    hashMap.put("idx",object.getJSONArray("datas").getJSONObject(i).get("manual_key").toString());
                    hashMap.put("file_nm",object.getJSONArray("datas").getJSONObject(i).get("file_nm").toString());
                    hashMap.put("file_size",object.getJSONArray("datas").getJSONObject(i).get("file_size").toString());
                    hashMap.put("data1",object.getJSONArray("datas").getJSONObject(i).get("manual_title").toString());
                    hashMap.put("data2",object.getJSONArray("datas").getJSONObject(i).get("manual_time").toString());
                    hashMap.put("data3",object.getJSONArray("datas").getJSONObject(i).get("file_nm").toString());
                    hashMap.put("data4","");
                    arrayList.add(hashMap);
                }
            } catch ( Exception e ) {
                Toast.makeText(getActivity(), "에러코드 Work 1", Toast.LENGTH_SHORT).show();
            }
        }else{
            Toast.makeText(getActivity(), "데이터가 없습니다.", Toast.LENGTH_SHORT).show();
        }
        mAdapter = new BoardAdapter(getActivity(), arrayList);
        listView.setAdapter(mAdapter);
    }

    @OnClick(R.id.top_search)
    public void getSearch() {
        if(layout.getVisibility()==View.GONE){
            layout.setVisibility(View.VISIBLE);
            layout.setFocusable(true);
        }else{
            layout.setVisibility(View.GONE);
        }
    }

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    //해당 검색값 데이터 조회
    @OnClick(R.id.button1)
    public void onSearchColumn() {
        //검색하면 키보드 내리기
        InputMethodManager imm= (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_search.getWindowToken(), 0);

        if(et_search.getText().toString().length()==0){
            Toast.makeText(getActivity(), "검색어를 입력하세요.", Toast.LENGTH_SHORT).show();
        }else{
            async_progress_dialog("getBoardInfo");
        }
    }

    //ListView의 item을 클릭했을 때.
    private class ListViewItemClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            fileNm= arrayList.get(position).get("data3").toString();
            fileSize= arrayList.get(position).get("file_size").toString();
            UtilClass.logD(TAG, "fileNm="+fileNm);
            alertDialog();
        }
    }

    public void alertDialog(){
        final android.app.AlertDialog.Builder alertDlg = new android.app.AlertDialog.Builder(getActivity());
        alertDlg.setTitle("선택하세요");
        alertDlg.setMessage(fileNm+"\n파일크기: "+fileSize);

        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("파일 다운", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                permissionlistener = new PermissionListener() {
                    @Override
                    public void onPermissionGranted() {
                        try {
                            downloadFile("http://119.202.60.106:8585/pdffile/"+ fileNm);
                        } catch (Exception e) {
                            if(pDlalog!=null) pDlalog.dismiss();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                        Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

                    }
                };
                new TedPermission(getActivity())
                        .setPermissionListener(permissionlistener)
                        .setRationaleMessage("파일을 다운받기 위해선 권한이 필요합니다.")
                        .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                        .setGotoSettingButtonText("권한확인")
                        .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .check();
            }
        });
        // '아니오' 버튼이 클릭되면
        alertDlg.setNegativeButton("파일 열기", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String filePath= fileDir  + fileNm;
                openFile(filePath.trim());
            }
        });
        alertDlg.show();
    }

    public void openFile(String contentsPath) {
        File file = new File(contentsPath);

        if(file.exists()) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                Uri uri = null;

                // So you have to use Provider
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(getActivity(), getActivity().getApplicationContext().getPackageName() + ".provider", file);

                    // Add in case of if We get Uri from fileProvider.
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                }else{
                    uri = Uri.fromFile(file);
                }
                intent.setDataAndType(uri, "application/pdf");
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("No Application Found");
                builder.setMessage("파일을 보기 위한 앱이 없습니다.\n앱을 다운 받기 위해 이동하시겠습니까?");
                builder.setPositiveButton("이동하기", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // TODO Auto-generated method stub

                        Intent marketIntent = new Intent(Intent.ACTION_VIEW);
                        marketIntent.setData(Uri.parse("market://details?id=com.adobe.reader"));
//                        marketIntent.setData(Uri.parse("market://details?id=com.infraware.office.link"));
                        startActivity(marketIntent);

                    }
                });
                builder.setNegativeButton("나중에", null);
                builder.create().show();

            }
        }else{
            Toast.makeText(getActivity(), "해당 파일이 없습니다.", Toast.LENGTH_SHORT).show();
        }
    }

    //파일 다운로드
    public void downloadFile(String fileUrl) {
        pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<ResponseBody> call = service.downloadFile(fileUrl);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    try {
                        //새로운 TaskFragment 생성
                        TaskFragment taskFragment = new TaskFragment();

                        //Task를 생성하여 taskFragment의 setTask메소드에서 Task 인스턴스를 저장하고, Task에게 taskFragment 인스턴스를 넘겨준다.
                        taskFragment.setTask(new FileDownProgressTask(response.body(),fileDir, fileNm));

                        //taskFragment에서 getTargetFragment().onActivityResult를 호출하면 MainFragment의 onActivityResult가 호출되도록 설정
                        taskFragment.setTargetFragment( WorkStandardMainFragment.this, DIALOG_REQUEST_CODE );

                        //프레그먼트를 보여준다.태그는 나중에 프레그먼트를 찾기 위해 사용 된다.
                        taskFragment.show(getFragmentManager(), TASK_FRAGMENT_TAG);
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }else{
                    UtilClass.logD(TAG, "response isFailed="+response);
                    Toast.makeText(getActivity(), "response isFailed", Toast.LENGTH_SHORT).show();
                }
                if(pDlalog!=null) pDlalog.dismiss();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if(pDlalog!=null) pDlalog.dismiss();
                UtilClass.logD(TAG, "onFailure="+call.toString()+", "+t);
                Toast.makeText(getActivity(), "onFailure downloadFile",Toast.LENGTH_LONG).show();
            }
        });

    }

    //taskFragment에서 getTargetFragment().onActivityResult를 호출하면 이 메소드가 호출된다.
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        UtilClass.logD(TAG, "onActivityResult="+ resultCode);
        if (requestCode == DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Boolean downloadResult = data.getExtras().getBoolean("downloadResult");
            UtilClass.logD(TAG, "onActivityResult="+downloadResult);
            if ( downloadResult) {
                Toast.makeText(getActivity(), "다운로드 완료", Toast.LENGTH_SHORT).show();
                String filePath= fileDir  + fileNm;
                openFile(filePath.trim());
            }
            else {
                Toast.makeText(getActivity(), "다운로드 실패", Toast.LENGTH_SHORT).show();
            }

        }else if (requestCode == DIALOG_REQUEST_CODE && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getActivity(), "다운로드가 중단되었습니다.", Toast.LENGTH_SHORT).show();
        }
    }


    public static class TaskFragment extends DialogFragment {
        final String TAG = "TaskFragment";
        FileDownProgressTask mTask;

        @Bind(R.id.progressBar) ProgressBar mProgressBar;
        @Bind(R.id.progressBarText) TextView mProgressBarText;

        public void setTask(FileDownProgressTask task) {
            mTask = task;

            //updateProgress()와 taskFinished() 처리가 이 프레그먼트에서 이루어짐을 AsyncTask에게 알려준다.
            mTask.setFragment(this);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            UtilClass.logD(TAG, "onCreate");

            //화면 회전으로 MainFragment와 MainFragment가 destroy되더라도 TaskFragment 인스턴스 유지시켜줌
            setRetainInstance(true);

            //Task를 시작한다.
            if (mTask != null)
                mTask.execute();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.fragment_dialog, container);
            ButterKnife.bind(this, view);
            getDialog().setTitle("Progress Dialog");

            //다이얼로그 밖을 터치하더라도 다이얼로그를 닫지 못하도록 설정
            getDialog().setCanceledOnTouchOutside(false);

            return view;
        }

        // This is to work around what is apparently a bug. If you don't have it
        // here the dialog will be dismissed on rotation, so tell it not to dismiss.
        @Override
        public void onDestroyView() {
            if (getDialog() != null && getRetainInstance())
                getDialog().setDismissMessage(null);
            super.onDestroyView();
        }

        //뒤로가기를 눌러서 다운로드를 취소한 경우, 다이얼로그가 사라진다.
        @Override
        public void onDismiss(DialogInterface dialog) {
            UtilClass.logD(TAG, "onDismiss getTarget= "+getTargetFragment());
            super.onDismiss(dialog);

            //Task가 동작중이라면 취소(cancel) 시킨다.
            if (mTask != null)
                mTask.cancel(true);

        }

        @Override
        public void onResume() {
            super.onResume();
            // This is a little hacky, but we will see if the task has finished while we weren't
            // in this activity, and then we can dismiss ourselves.
            if (mTask == null)
                dismiss();
        }

        //다운로드 진행상태를 프로그레스바에 표시하기 위해 AsyncTask에서 호출한다.
        public void updateProgress(String... progressState) {
            mProgressBar.setProgress(Integer.parseInt(progressState[0]));
            mProgressBarText.setText(progressState[1]);
//            UtilClass.logD( TAG, "TaskFragment:updateProgress "+ progressState[1]);
        }

        public  void taskCanceled() {
            if (isResumed())
                dismiss();

            mTask = null;

            //결과값을 MainFragment에게 전달한다.
            if (getTargetFragment() != null) {
                Intent data = new Intent();
                boolean downloadResult = false;
                data.putExtra("downloadResult", downloadResult);
                getTargetFragment().onActivityResult( getTargetRequestCode(), Activity.RESULT_CANCELED, data );
            }
        }

        //다운로드가 완료된 경우 AsyncTask에서 호출한다.
        public void taskFinished(Boolean downloadResult) {
            // Make sure we check if it is resumed because we will crash if trying to dismiss the dialog
            // after the user has switched to another app.
            if (isResumed())
                dismiss();

            // If we aren't resumed, setting the task to null will allow us to dimiss ourselves in
            // onResume().
            mTask = null;

            //결과값을 MainFragment에게 전달한다.
            if (getTargetFragment() != null) {
                Intent data = new Intent();
                data.putExtra ("downloadResult", downloadResult);
                getTargetFragment().onActivityResult( getTargetRequestCode(), Activity.RESULT_OK, data);
            }
        }
    }

}
