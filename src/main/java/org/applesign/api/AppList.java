package org.applesign.api;

import org.applesign.utils.HttpUtils;
import org.applesign.utils.MD5Sign;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;

import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppList {
    private static final String account = "test"; // 用户名
    private static final String passwd = "980980"; // 用户登录密码 作为加密的盐值
    private static final String API_APPLIST = "https://applesign.org/api/third/appList";

    private void list()
    {
        long timestamp = System.currentTimeMillis() / 1000;

        String appName = ""; // 模糊查询

        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("account",account);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("name",appName);
        params.put("size", "10");
        params.put("current", "1");
        String newSign = MD5Sign.getSign(params,passwd);
        params.put("secret",newSign);

        try {
            System.out.println(params.toString());
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type","application/json;charset=utf-8");
            HttpResponse httpResponse = HttpUtils.doPost(API_APPLIST, null, headers, params, new HashMap<>());
            String respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(respStr);
            Header[] responseAllHeaders = httpResponse.getAllHeaders();
            System.out.println(responseAllHeaders);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args){
        AppList tl = new AppList();
        tl.list();
    }
}
