package com.sukaiyi.bandwagonvps;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.orhanobut.logger.Logger;
import com.sukaiyi.bandwagonvps.net.ApiGate;
import com.sukaiyi.bandwagonvps.utils.Switch;

import org.json.JSONObject;
import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import cz.msebera.android.httpclient.Header;

/**
 * Example local unit dialog_confirm_change_os, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void TEST() throws Exception {
        System.out.print(Switch.b2Any(15995800));
    }

    @Test
    public void testAPI(){
        RequestParams params = new RequestParams();
        params.put("veid", "496375");
        params.put("api_key", "private_Uo82ohdP9guSmbm5VDzRE3mK");
        ApiGate.get("getRawUsageStats", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                super.onSuccess(statusCode, headers, response);
                Logger.d(response);
            }
        });
    }

    @Test
    public void test(){
        SimpleDateFormat format = new SimpleDateFormat();
        format.setLenient(true);
        try {
            System.out.println(format.parse("07/10/96 4:5 PM,PDT"));
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}