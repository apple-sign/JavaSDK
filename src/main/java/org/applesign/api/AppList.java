package org.applesign.api;

import org.applesign.Credentials;
import org.applesign.utils.HttpUtils;
import org.applesign.utils.MD5Sign;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.apache.commons.cli.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppList {

    private static final String API_APPLIST = "https://applesign.org/api/third/appList";

    private void list(String account, String passwd)
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
            System.out.println(params);
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type","application/json;charset=utf-8");
            HttpResponse httpResponse = HttpUtils.doPost(API_APPLIST, null, headers, params, new HashMap<>());
            String respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(respStr);
            Header[] responseAllHeaders = httpResponse.getAllHeaders();
            System.out.println(Arrays.toString(responseAllHeaders));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
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
        AppList tl = new AppList();
        tl.list(account, passwd);
    }
}
