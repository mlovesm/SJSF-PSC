package com.creative.psc.app.util;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;

import com.creative.psc.app.work.WorkStandardMainFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;

/**
 * Created by GS on 2017-09-05.
 */

public class FileDownProgressTask extends AsyncTask<Boolean, String, Boolean>{
    private static final String TAG = "FileDownProgressTask";

    private PowerManager.WakeLock mWakeLock;
    WorkStandardMainFragment.TaskFragment mFragment;

    ResponseBody responseBody;
    String fileDir;
    String fileNm;

    public FileDownProgressTask(ResponseBody responseBody, String fileDir, String fileNm) {
        this.responseBody = responseBody;
        this.fileDir = fileDir;
        this.fileNm = fileNm;
    }

    public void setFragment(WorkStandardMainFragment.TaskFragment fragment) {
        mFragment = fragment;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        //사용자가 다운로드 중 파워 버튼을 누르더라도 CPU가 잠들지 않도록 해서
        //다시 파워버튼 누르면 그동안 다운로드가 진행되고 있게 됩니다.
        PowerManager pm = (PowerManager) mFragment.getActivity().getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        mWakeLock.acquire();

    }

    @Override
    protected Boolean doInBackground(Boolean... booleen) {
        boolean writtenToDisk = writeResponseBodyToDisk(responseBody, fileDir, fileNm);
        UtilClass.logD(TAG, "file download was a success? " + writtenToDisk);

        return writtenToDisk;
    }

    @Override
    protected void onProgressUpdate(String... progressState) {
        if (mFragment == null)
            return;
        mFragment.updateProgress(progressState);
    }

    //다운로드 중단
    @Override
    protected void onCancelled() {
        UtilClass.logD(TAG, "onCancelled");
        if (mFragment == null)
            return;
        mFragment.taskCanceled();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (mFragment == null)
            return;
        mFragment.taskFinished(result);
    }

    public boolean writeResponseBodyToDisk(ResponseBody body, String fileDir, String fileNm) {
        try {
            // todo change the file location/name according to your needs
            String externalState = Environment.getExternalStorageState();
//            UtilClass.logD(TAG, "경로1="+Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator  + "Download" + File.separator  + fileNm);
//            UtilClass.logD(TAG, "경로2="+getActivity().getExternalFilesDir(null).getAbsolutePath()+ File.separator + fileNm);
            File createFile = new File(fileDir  + fileNm);

            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(createFile);

                while (true) {
                    //사용자가 BACK 버튼 누르면 취소가능
                    if (isCancelled()) {
                        inputStream.close();

                        return false;
                    }
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
//                    UtilClass.logD(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                    if (fileSize > 0) {
                        int percent = (int)(((float)fileSizeDownloaded/fileSize) * 100);
                        String progressText = "Downloading.. " + fileSizeDownloaded + "KB / " + fileSize + "KB (" + (int)percent + "%)";
                        publishProgress(String.valueOf(percent), progressText);
                    }
                }
                outputStream.flush();

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
