package org.applesign.api;

import com.alibaba.fastjson.*;
import org.apache.commons.cli.*;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.applesign.Credentials;
import org.applesign.utils.HttpUtils;
import org.applesign.utils.MD5Sign;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class AuthCode {

    private static final String API_AUTHCODE = "https://applesign.org/api/third/authcode";
    private static final String API_LISTCODE = "https://applesign.org/api/third/authcodeList";

    public String generate(String account, String passwd, String alias, String udid)
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
            HttpResponse httpResponse = HttpUtils.doPost(API_AUTHCODE, null, headers, params, new HashMap<>());
            String respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(respStr);
            // return "";
            JSONObject obj = JSON.parseObject(respStr);
            return obj.getJSONObject("data").getString("code");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "";
    }


    public JSONObject list(String account, String passwd, Long appId, String udid)
    {
        long timestamp = System.currentTimeMillis() / 1000;

        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("account",account);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("appId", String.valueOf(appId));
        params.put("udid", udid);
        params.put("size", "1500");
        params.put("current", "1");
        String newSign = MD5Sign.getSign(params,passwd);
        params.put("secret",newSign);

        try {
            // System.out.println(params);
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type","application/json;charset=utf-8");
            HttpResponse httpResponse = HttpUtils.doPost(API_LISTCODE, null, headers, params, new HashMap<>());
            String respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(respStr);
            JSONObject obj = JSON.parseObject(respStr);
            return obj.getJSONObject("data");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public static void main(String[] args) throws IOException {
        String account = Credentials.account;
        String passwd = Credentials.passwd;
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
        boolean ACTION_LIST = (args.length > 2);
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
        FileWriter writer = new FileWriter("AuthCode-" + account + ".txt", false);
        if (ACTION_LIST) {
            JSONObject data = ac.list(account, passwd, 155151184953346L, "");
            JSONArray list = data.getJSONArray("list");
            for (int i=0; i<list.size(); i++) {
                writer.write(list.getJSONObject(i).getString("code") + "\r\n");
                writer.flush();
            }
        }
        else {
            for (int i=0; i<1500; i++) {
                String code = ac.generate(account, passwd, Credentials.alias, "");
                writer.write(code + "\r\n");
                writer.flush();
            }
        }
        writer.close();
    }
}
