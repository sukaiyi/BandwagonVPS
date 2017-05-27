package com.sukaiyi.bandwagonvps.net;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

/**
 * Created by sukaiyi on 2017/05/14.
 */

public class ApiGate {
    private static final String BASE_URL = "https://api.64clouds.com/v1/";
        private static AsyncHttpClient client = new AsyncHttpClient();

    private ApiGate(){

    }

    public static void get(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void cancelAllRequests(){
        client.cancelAllRequests(true);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
