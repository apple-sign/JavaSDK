package org.applesign.api;

import org.apache.commons.cli.*;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.applesign.utils.HttpUtils;
import org.applesign.utils.MD5Sign;
// import com.alibaba.fastjson.JSON;

import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class AuthCode {

    private static final String API_APPLIST = "https://applesign.org/api/third/authcode";

    private String generate(String account, String passwd, String alias, String udid)
    {
        long timestamp = System.currentTimeMillis() / 1000;

        String appName = ""; // 模糊查询

        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("account",account);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("name",appName);
        params.put("alias", alias);
        params.put("udid", udid);
        String newSign = MD5Sign.getSign(params,passwd);
        params.put("secret",newSign);

        try {
            // System.out.println(params);
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type","application/json;charset=utf-8");
            HttpResponse httpResponse = HttpUtils.doPost(API_APPLIST, null, headers, params, new HashMap<>());
            String respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(respStr);
            return "";
            // JSONObject obj = JSON.parseObject(respStr);
            // return obj.getString("code");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }

    public static void main(String[] args){
        String account = "test"; // 用户名
        String passwd = "980980"; // 用户登录密码 作为加密的盐值
        Options options = new Options();
        Option userO = new Option("u", "user", true, "Username");
        userO.setRequired(true);
        options.addOption(userO);

        Option passwdO = new Option("p", "passwd", true, "Password");
        passwdO.setRequired(true);
        options.addOption(passwdO);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        System.out.println(Arrays.toString(args));
        if (args.length > 2) {
            try {
                cmd = parser.parse(options, args);
                account = cmd.getOptionValue("user");
                passwd = cmd.getOptionValue("passwd");
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("utility-name", options);
                System.exit(1);
            }
        }
        AuthCode ac = new AuthCode();
        for (int i=0; i<10; i++) {
            ac.generate(account, passwd, "C5mr", "");
        }
    }
}
