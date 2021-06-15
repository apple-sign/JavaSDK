package org.applesign;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.applesign.api.AppList;
import org.applesign.api.AppUpload;

import java.util.Arrays;
import java.util.Locale;

public class CLI {

    public static void main(String[] args) {
        String account = Credentials.account;
        String passwd = Credentials.passwd;
        String ipaPath = "";
        Options options = new Options();
        Option userH = new Option("h", "host", true, "Host");
        userH.setRequired(false);
        options.addOption(userH);

        Option userO = new Option("u", "user", true, "Username");
        userO.setRequired(true);
        options.addOption(userO);

        Option passwdO = new Option("p", "passwd", true, "Password");
        passwdO.setRequired(true);
        options.addOption(passwdO);

        Option ipaO = new Option("i", "ipa", true, "ipa");
        ipaO.setRequired(true);
        options.addOption(ipaO);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;
        System.out.println(Arrays.toString(args));
        if (args.length > 2) {
            try {
                cmd = parser.parse(options, args);
                account = cmd.getOptionValue("user");
                passwd = cmd.getOptionValue("passwd");
                ipaPath = cmd.getOptionValue("ipa");
                String host = cmd.getOptionValue("host");
                if (StringUtils.isNotEmpty(host)) {
                    host = host.toLowerCase(Locale.ROOT);
                    if (host.startsWith("http")) {
                        Credentials.API_HOST = host;
                    } else {
                        Credentials.API_HOST = "https://" + host;
                    }
                }
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("utility-name", options);
                System.exit(1);
            }
        }
        if (StringUtils.isNotEmpty(ipaPath)) {
            AppUpload tl = new AppUpload();
            System.out.println("Uploading IPA from " + ipaPath + " to " + account);
            tl.upload(account, passwd, ipaPath);
            return;
        }

        AppList tl = new AppList();
        JSONObject apps = tl.list(account, passwd);
        System.out.println("Apps: " + JSON.toJSONString(apps));

    }
}
