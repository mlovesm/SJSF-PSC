package com.creative.psc.app.inno;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.androidquery.AQuery;
import com.androidquery.callback.AjaxCallback;
import com.androidquery.callback.AjaxStatus;
import com.creative.psc.app.R;
import com.creative.psc.app.fragment.ActivityResultEvent;
import com.creative.psc.app.fragment.BusProvider;
import com.creative.psc.app.menu.MainFragment;
import com.creative.psc.app.retrofit.Datas;
import com.creative.psc.app.retrofit.RetrofitService;
import com.creative.psc.app.util.FilePath;
import com.creative.psc.app.util.SettingPreference;
import com.creative.psc.app.util.UtilClass;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.squareup.otto.Subscribe;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class IdeaScribWriteFragment extends Fragment {
    private static final String TAG = "IdeaScribWriteFragment";
    private RetrofitService service;

    private static final String INSERT_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Inno/ideaScribWrite";
    private static String MODIFY_VIEW_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Inno/ideaScribDetail";
    private static String MODIFY_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Inno/ideaScribModify";
    private static String DELETE_URL = MainFragment.ipAddress+ MainFragment.contextPath+"/rest/Inno/ideaScribDelete";

    private String mode="";
    private String idx="";
    private String dataSabun;

    @Bind(R.id.top_title) TextView textTitle;
    @Bind(R.id.editText1) EditText et_title;
    @Bind(R.id.editText2) EditText et_etc;
    @Bind(R.id.textView1) TextView tv_fileName;
    @Bind(R.id.textView2) TextView tv_writerName;

    //파일,앨범,촬영 선택 업로드 관련
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_ALBUM = 2;
    private static final int CROP_FROM_IMAGE = 3;
    private static final int FROM_FILE = 4;
    private boolean fileDown= false;

    final int RESULT_OK=-1;
    private String filePath;
    private String fileName;
    private String fileOrgName;
    private String modifyFileName;
    private String modifyFileOrgName;

    private ProgressDialog dialog;

    private String mCurrentPhotoPath;
    private Uri photoURI, albumURI, fileURI = null;
    private Boolean album =false;

    private SettingPreference pref;
    private PermissionListener permissionlistener;
    private AQuery aq;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.idea_scrib_write, container, false);
        ButterKnife.bind(this, view);
        aq = new AQuery( getActivity() );
        service= RetrofitService.rest_api.create(RetrofitService.class);

        mode= getArguments().getString("mode");
        if(mode==null) mode="";

        if(mode.equals("insert")){
            dataSabun= MainFragment.loginSabun;
            view.findViewById(R.id.linear2).setVisibility(View.GONE);
            textTitle.setText("아이디어낙서방 작성");
            tv_writerName.setText(MainFragment.loginName);
        }else{
            textTitle.setText("아이디어낙서방 수정");
            idx= getArguments().getString("idea_key");
            async_progress_dialog("getBoardDetailInfo");
            fileDown= true;
        }
        view.findViewById(R.id.top_save).setVisibility(View.VISIBLE);

        pref = new SettingPreference("loginData",getActivity());

        return view;
    }//onCreateView

    @OnClick(R.id.top_home)
    public void goHome() {
        UtilClass.goHome(getActivity());
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onDestroyView() {
        BusProvider.getInstance().unregister(this);
        super.onDestroyView();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "unregister Called.");
//
//        getActivity().unregisterReceiver(receiverNotificationClicked);
//        receiverNotificationClicked = null;
//
//        getActivity().unregisterReceiver(receiverDownloadComplete);
//        receiverDownloadComplete = null;
    }

    @OnClick(R.id.imageButton1)
    public void imageUpload(){
        permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
//                Toast.makeText(getApplicationContext(), "권한 허가", Toast.LENGTH_SHORT).show();
                if(MainFragment.loginSabun.equals(dataSabun)){
                    imagesChoice();
                }else{
                    Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(getActivity(), "권한 거부 목록\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();

            }
        };
        new TedPermission(getActivity())
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("파일/촬영 업로드를 위해선 권한이 필요합니다.")
                .setDeniedMessage("권한을 확인하세요.\n\n [설정] > [애플리케이션] [해당앱] > [권한]")
                .setGotoSettingButtonText("권한확인")
//                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA)
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

//    @OnClick(R.id.textView1)
//    public void fileDown(){
//        if(fileDown){
//            final AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
//            alertDlg.setTitle("알림");
//
//            // '예' 버튼이 클릭되면
//            alertDlg.setPositiveButton("파일받기", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
////                    new FileDownTask().execute();
//                    downManager();
//                }
//            });
//            // '아니오' 버튼이 클릭되면
//            alertDlg.setNeutralButton("취소", new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(DialogInterface dialog, int which) {
//
//                }
//            });
//            alertDlg.show();
//
//        }else{
//
//        }
//    }

    private void imagesChoice(){
        AlertDialog.Builder alertDlg = new AlertDialog.Builder(getActivity());
        alertDlg.setTitle("선택하세요.")
                .setCancelable(true);

        alertDlg.setPositiveButton("촬영", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getPhotoFromCamera();
            }
        });
        alertDlg.setNegativeButton("파일", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int paramInt) {
                getFileList();
            }
        });

//        alertDlg.setMessage("?");
        alertDlg.show();
    }

    private void getFileList() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");

        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to upload "), FROM_FILE);
    }

    private void getPhotoFromCamera() { // 카메라 촬영 후 이미지 가져오기
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if(getActivity() != null) {
            File photoFile = createImageFile();      //사진 찍은 후 임시파일 저장

            if(photoFile != null){
                photoURI = Uri.fromFile(photoFile); //임시 파일의 위치,경로 가져옴
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI); //임시 파일 위치에 저장
                getActivity().startActivityForResult(intent, PICK_FROM_CAMERA);
            }
        }
    }

    private void getPhotoFromGallery() { // 갤러리에서 이미지 가져오기
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_PICK);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select file to upload "), PICK_FROM_ALBUM);
    }

    private File createImageFile() {
        String imageFileName = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        File storageDir = new File(Environment.getExternalStorageDirectory()+"/File/", imageFileName);
        mCurrentPhotoPath = storageDir.getAbsolutePath();
        return storageDir;
    }

    private void cropImage() {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(photoURI, "image/*");

//        intent.putExtra("outputX", 200);
//        intent.putExtra("outputY", 200);
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        intent.putExtra("scale", true);

        if(album == false) {
            intent.putExtra("output",photoURI);
            UtilClass.logD(TAG,"photoURI="+photoURI);
        }else{
            intent.putExtra("output",albumURI);
            UtilClass.logD(TAG,"albumURI="+albumURI);
        }
        getActivity().startActivityForResult(intent, CROP_FROM_IMAGE);
    }

    /**
     * Fragment에서 startactivityForresult실행시 fragment에 들어오지 않는 문제
     *
     * @param activityResultEvent
     */
    @Subscribe
    public void onActivityResultEvent(ActivityResultEvent activityResultEvent){
        UtilClass.logD(TAG, "activeResultEvent="+activityResultEvent);
        onActivityResult(activityResultEvent.getRequestCode(), activityResultEvent.getResultCode(), activityResultEvent.getData());
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        if (resultCode != RESULT_OK) return;

        switch (requestCode){
            case FROM_FILE:{
                fileURI = data.getData();
                filePath= FilePath.getPath(getActivity(), fileURI);
                UtilClass.logD(TAG, "filePath? "+filePath);

                int lastIndexOf = filePath.lastIndexOf("/");
                fileName= filePath.substring(lastIndexOf+1, filePath.length());

//                new ImageUploadTask().execute();
//                new NetworkCall().execute("로그인");
                fileUpload();

                break;
            }

            case  PICK_FROM_ALBUM:{
                album = true;
                File albumFile  = createImageFile();
                if(albumFile != null){
                    albumURI = Uri.fromFile(albumFile);
                }
                photoURI = data.getData();  //앨범이미지 경로
            }

            case PICK_FROM_CAMERA:{
                cropImage();
                break;
            }

            case CROP_FROM_IMAGE:{
                Bitmap photo = BitmapFactory.decodeFile(photoURI.getPath());    //크롭된 이미지
                Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);  //동기화

                if(album == false) {
                    intent.setData(photoURI);
                    filePath= photoURI.getPath();
                }else{
                    album= false;
                    intent.setData(albumURI);
                    filePath= albumURI.getPath();
                }

                // 임시 파일 삭제
//                File f = new File(filePath);
//                if(f.exists()) {
//                    f.delete();
//                }

                UtilClass.logD(TAG,"filePath="+filePath);
                int lastIndexOf = filePath.lastIndexOf("/");
                fileName= filePath.substring(lastIndexOf+1, filePath.length());

                getActivity().sendBroadcast(intent);

                break;
            }
        }
    }

    public void fileUpload(){
        service = RetrofitService.rest_api.create(RetrofitService.class);

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        File file = new File(filePath);
        RequestBody reqFile = RequestBody.create(MediaType.parse("*/*"), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("fileData", file.getName(), reqFile);

        Call<Datas> call = service.fileUpload(body);
        call.enqueue(new Callback<Datas>() {
            @Override
            public void onResponse(Call<Datas> call, Response<Datas> response) {
                UtilClass.logD(TAG, "response="+response);
                if (response.isSuccessful()) {
                    UtilClass.logD(TAG, "isSuccessful="+response.body().toString());
                    String status= response.body().getStatus();
                    try {
                        if(status.equals("success")){
                            tv_fileName.setText(response.body().getList().get(0).get("file_org_nm"));
                            tv_fileName.setTextColor(Color.rgb(48,48,208));
                        }

                    } catch ( Exception e ) {
                        e.printStackTrace();
                        Toast.makeText(getActivity(), "에러코드 fileUpload", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getActivity(), "onFailure fileUpload",Toast.LENGTH_SHORT).show();
            }
        });

    }

    public void async_progress_dialog(String callback){
        ProgressDialog dialog = ProgressDialog.show(getActivity(), "", "Loading...", true, true);
        dialog.setInverseBackgroundForced(false);

        String url = null;
        if(callback.equals("searchUserData")){
            
        }else if(callback.equals("getBoardDetailInfo")){
            url= MODIFY_VIEW_URL+"/"+idx;
            aq.ajax( url, null, JSONObject.class, new AjaxCallback<JSONObject>() {
                @Override
                public void callback(String url, JSONObject object, AjaxStatus status ) {
                    if( object != null) {
                        try {
                            dataSabun= object.getJSONArray("datas").getJSONObject(0).get("input_id").toString();
                            if(MainFragment.loginSabun.equals(dataSabun)){
                            }else{
                                et_title.setFocusableInTouchMode(false);
                                et_etc.setFocusableInTouchMode(false);
                                getActivity().findViewById(R.id.linear1).setVisibility(View.GONE);
                                getActivity().findViewById(R.id.linear2).setVisibility(View.GONE);
                            }
                            et_title.setText(object.getJSONArray("datas").getJSONObject(0).get("idea_title").toString());
                            et_etc.setText(object.getJSONArray("datas").getJSONObject(0).get("idea_etc").toString());
                            tv_writerName.setText(object.getJSONArray("datas").getJSONObject(0).get("input_nm").toString());

                            String file_org_nm= object.getJSONArray("datas").getJSONObject(0).get("file_org_nm").toString();
                            if(!file_org_nm.equals("null")){
                                tv_fileName.setText(file_org_nm);
                                tv_fileName.setTextColor(Color.rgb(48,48,208));
                                fileOrgName= file_org_nm;
                                modifyFileName= object.getJSONArray("datas").getJSONObject(0).get("file_nm").toString();
                                fileName= modifyFileName;
                            }else{
                                fileDown= false;
                            }

                        } catch ( Exception e ) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "에러코드 Idea 2", Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        Log.d(TAG,"Data is Null");
                        Toast.makeText(getActivity(),"데이터가 없습니다.",Toast.LENGTH_SHORT).show();
                    }
                }
            } );
        }else{

        }
        aq.progress(dialog).ajax(null, JSONObject.class, this, callback);

    }

    @OnClick({R.id.textButton1, R.id.top_save})
    public void alertDialogSave(){
        if(MainFragment.loginSabun.equals(dataSabun)){
            alertDialog("S");
        }else{
            Toast.makeText(getActivity(),"작성자만 가능합니다.", Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick({R.id.textButton2})
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
        }else{
            alertDlg.setMessage("삭제하시겠습니까?");
        }
        // '예' 버튼이 클릭되면
        alertDlg.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(gubun.equals("S")){
                    postData();
                }else{
                    deleteData();
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
        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call = service.deleteData("Inno","ideaScribDelete", idx);

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
        String push_text = et_etc.getText().toString();

        if (et_title.equals("") || et_title.length()==0) {
            Toast.makeText(getActivity(), "빈칸을 채워주세요.",Toast.LENGTH_LONG).show();
            return;
        }

        Map<String, Object> map = new HashMap();
        map.put("writer_sabun", MainFragment.loginSabun);
        map.put("writer_name", MainFragment.loginName);
        map.put("idea_title",push_title);
        map.put("idea_etc",push_text);
        map.put("fileYN","false");

        final ProgressDialog pDlalog = new ProgressDialog(getActivity());
        UtilClass.showProcessingDialog(pDlalog);

        Call<Datas> call= null;
        if(mode.equals("insert")){
            call = service.insertData("Inno","ideaScribWrite", map);
        }else{
            call = service.updateData("Inno","ideaScribModify", idx, map);
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


    /*
    *   파일 다운로드 매니저
    * */

    private long mDownloadReference;
    private DownloadManager mDownloadManager;
    private BroadcastReceiver receiverDownloadComplete;    //다운로드 완료 체크
    private BroadcastReceiver receiverNotificationClicked;    //다운로드 시작 체크


    public void downManager(){
        if(mDownloadManager == null) {
            mDownloadManager = (DownloadManager) getActivity().getSystemService(Context.DOWNLOAD_SERVICE);
        }

        try {
            Uri uri = Uri.parse(MainFragment.ipAddress+"/uploadFile/"+fileName);        //data는 파일을 떨궈 주는 uri
            UtilClass.logD(TAG, "uri="+uri);
            DownloadManager.Request request = new DownloadManager.Request(uri);
            request.setTitle("파일다운로드");    //다운로드 완료시 noti에 제목

            request.setVisibleInDownloadsUi(true);
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);

            //모바일 네트워크와 와이파이일때 가능하도록
            request.setNotificationVisibility( DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

            //다운로드 완료시 noti에 보여주는것
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS , fileOrgName);
            //다운로드 경로, 파일명을 적어준다
            mDownloadReference = mDownloadManager.enqueue(request);

        }catch (Exception e){
            e.printStackTrace();
            Toast.makeText(getActivity(),"파일 다운로드 실패", Toast.LENGTH_SHORT).show();

        }
        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED);

        receiverNotificationClicked = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String extraId = DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS;
                long[] references = intent.getLongArrayExtra(extraId);
                for (long reference : references) {

                }
            }
        };

        getActivity().registerReceiver(receiverNotificationClicked, filter);


        IntentFilter intentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);

        receiverDownloadComplete = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {

                long reference = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);

                if(mDownloadReference == reference){

                    DownloadManager.Query query = new DownloadManager.Query();
                    query.setFilterById(reference);
                    Cursor cursor = mDownloadManager.query(query);

                    cursor.moveToFirst();

                    int columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                    int columnReason = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

                    int status = cursor.getInt(columnIndex);
                    int reason = cursor.getInt(columnReason);

                    cursor.close();

                    switch (status){

                        case DownloadManager.STATUS_SUCCESSFUL :

                            Toast.makeText(getActivity(), "다운로드 완료", Toast.LENGTH_SHORT).show();
                            break;

                        case DownloadManager.STATUS_PAUSED :

                            Toast.makeText(getActivity(), "다운로드 중지 : " + reason, Toast.LENGTH_SHORT).show();
                            break;

                        case DownloadManager.STATUS_FAILED :

                            Toast.makeText(getActivity(), "다운로드 취소 : " + reason, Toast.LENGTH_SHORT).show();
                            break;
                    }
                }
            }
        };

        getActivity().registerReceiver(receiverDownloadComplete, intentFilter);
    }


}
