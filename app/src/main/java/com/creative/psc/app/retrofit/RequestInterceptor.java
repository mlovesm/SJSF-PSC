package com.creative.psc.app.retrofit;

import com.creative.psc.app.util.UtilClass;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by GS on 2017-08-07.
 */
public class RequestInterceptor  implements Interceptor {
    private static final String TAG = "RequestInterceptor";

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request.Builder builder = chain.request().newBuilder();

        builder.addHeader("comp_database", "");
        UtilClass.logD(TAG, "builder="+builder);

        return chain.proceed(builder.build());
    }
}
