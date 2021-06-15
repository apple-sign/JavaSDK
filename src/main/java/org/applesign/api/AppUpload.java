package org.applesign.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.cli.*;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.applesign.Credentials;
import org.applesign.utils.HttpUtils;
import org.applesign.utils.MD5Sign;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

public class AppUpload {
    private static String API_HOST = "https://applesign.org";
    private static final String API_OSS_CONFIG = "/api/third/ossUpload";
    private static final String API_OSS_PARSE = "/api/third/ossParse";

    /**
     * https://help.aliyun.com/document_detail/84781.htm
     *
     * @param ossCfg  {"key":"156611628892161/ios/1622038995344.ipa","policy":"eyJleHBpcmF0aW9uIjoiMjAyMS0wNS0yNlQxNToyMzoxNS4zNTFaIiwiY29uZGl0aW9ucyI6W1siY29udGVudC1sZW5ndGgtcmFuZ2UiLDAsMjE0NzQ4MzY0OF0sWyJzdGFydHMtd2l0aCIsIiRrZXkiLCIiXV19","signature":"PjQ4N+bO5/TyrkyLxhIw2cJdoGY=","host":"https://cjq-ipa-private.oss-accelerate.aliyuncs.com/","ossaccessKeyId":"LTAI4G2yMKDveTsS3L******"}
     * @param file
     */
    private String uploadToOSS(JSONObject ossCfg, File file) throws Exception {
        String endpoint = ossCfg.getString("host");
        String accessKeyId = ossCfg.getString("ossaccessKeyId");
        String key = ossCfg.getString("key");
        String[] keyPath = key.split("/");
        // form fields
        Map<String, String> formFields = new LinkedHashMap<String, String>();

        // key
        formFields.put("key", key);
        // Content-Disposition
        formFields.put("Content-Disposition", "attachment;filename=" + keyPath[keyPath.length - 1]);
        // OSSAccessKeyId
        formFields.put("OSSAccessKeyId", accessKeyId);
        // policy
        formFields.put("policy", ossCfg.getString("policy"));
        // Signature
        formFields.put("Signature", ossCfg.getString("signature"));

        String ret = formUpload(endpoint, formFields, file);

        System.out.println("Post Object [" + key + "] to OSS");
        System.out.println("post reponse:" + ret);
        return ret;
    }

    private static String formUpload(String urlStr, Map<String, String> formFields, File file)
            throws Exception {
        String res = "";
        HttpURLConnection conn = null;
        String boundary = "9431149156168";

        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows; U; Windows NT 6.1; zh-CN; rv:1.9.2.6)");
            // Set Content-MD5. The MD5 value is calculated based on the whole message body.
            conn.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            OutputStream out = new DataOutputStream(conn.getOutputStream());

            // text
            if (formFields != null) {
                StringBuffer strBuf = new StringBuffer();
                Iterator<Map.Entry<String, String>> iter = formFields.entrySet().iterator();
                int i = 0;

                while (iter.hasNext()) {
                    Map.Entry<String, String> entry = iter.next();
                    String inputName = entry.getKey();
                    String inputValue = entry.getValue();

                    if (inputValue == null) {
                        continue;
                    }

                    if (i == 0) {
                        strBuf.append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\""
                                + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    } else {
                        strBuf.append("\r\n").append("--").append(boundary).append("\r\n");
                        strBuf.append("Content-Disposition: form-data; name=\""
                                + inputName + "\"\r\n\r\n");
                        strBuf.append(inputValue);
                    }

                    i++;
                }
                out.write(strBuf.toString().getBytes());
            }

            // file
            String filename = file.getName();
            String contentType = "application/octet-stream";

            StringBuffer strBuf = new StringBuffer();
            strBuf.append("\r\n").append("--").append(boundary)
                    .append("\r\n");
            strBuf.append("Content-Disposition: form-data; name=\"file\"; "
                    + "filename=\"" + filename + "\"\r\n");
            strBuf.append("Content-Type: " + contentType + "\r\n\r\n");

            out.write(strBuf.toString().getBytes());

            DataInputStream in = new DataInputStream(new FileInputStream(file));
            int bytes = 0;
            byte[] bufferOut = new byte[1024];
            while ((bytes = in.read(bufferOut)) != -1) {
                out.write(bufferOut, 0, bytes);
            }
            in.close();

            byte[] endData = ("\r\n--" + boundary + "--\r\n").getBytes();
            out.write(endData);
            out.flush();
            out.close();

            // Gets the file data
            strBuf = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                strBuf.append(line).append("\n");
            }
            res = strBuf.toString();
            reader.close();
            reader = null;
        } catch (Exception e) {
            System.err.println("Send post request exception: " + e);
            throw e;
        } finally {
            if (conn != null) {
                conn.disconnect();
                conn = null;
            }
        }

        return res;
    }

    public int upload(String account, String passwd, String ipaPath) {
        File file = new File(ipaPath);
        boolean exists =      file.exists();      // Check if the file exists
        boolean isDirectory = file.isDirectory(); // Check if it's a directory
        boolean isFile =      file.isFile();      // Check if it's a regular file
        if (!exists) {
            System.out.println("文件/文件夹不存在 " + ipaPath);
            return -1;
        }
        if (isFile) {
            if (ipaPath.toLowerCase().endsWith(".ipa")) {
                upload(account, passwd, file, 0, "ipa");
                return 1;
            } else {
                System.out.println("文件类型不匹配，忽略 " + ipaPath);
                return -2;
            }
        }
        if (isDirectory) {
            File[] directoryListing = file.listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    upload(account, passwd, child.getAbsolutePath());
                }
            }
            return 2;
        }
        System.out.println("未知路径 " + ipaPath);
        return -3;
    }

    public void upload(String account, String passwd, File ipaPath, long appId, String type) {
        long timestamp = System.currentTimeMillis() / 1000;

        SortedMap<String, String> params = new TreeMap<String, String>();
        params.put("account", account);
        params.put("timestamp", String.valueOf(timestamp));
        params.put("appId", String.valueOf(appId));
        params.put("type", type);
        String newSign = MD5Sign.getSign(params, passwd);
        params.put("secret", newSign);

        try {
            System.out.println(params);
            HashMap<String, String> headers = new HashMap<String, String>();
            headers.put("Content-Type", "application/json;charset=utf-8");
            HttpResponse httpResponse = HttpUtils.doPost(API_HOST + API_OSS_CONFIG, null, headers, params, new HashMap<>());
            String respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(respStr);
            Header[] responseAllHeaders = httpResponse.getAllHeaders();
            System.out.println(Arrays.toString(responseAllHeaders));

            // Upload to OSS
            JSONObject obj = JSON.parseObject(respStr);
            JSONObject ossCfg = obj.getJSONObject("data");
            uploadToOSS(ossCfg, ipaPath);

            // Upload to sign server
            timestamp = System.currentTimeMillis() / 1000;
            params = new TreeMap<String, String>();
            params.put("account", account);
            params.put("timestamp", String.valueOf(timestamp));
            params.put("appId", String.valueOf(appId));
            params.put("type", type);
            params.put("filename", ossCfg.getString("key"));
            newSign = MD5Sign.getSign(params, passwd);
            params.put("secret", newSign);
            httpResponse = HttpUtils.doPost(API_HOST + API_OSS_PARSE, null, headers, params, new HashMap<>());
            respStr = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(API_OSS_PARSE + " -> " + respStr);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static void main(String[] args) {
        String account = Credentials.account;
        String passwd = Credentials.passwd;
        String ipaPath = Credentials.ipaPath;
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
                        API_HOST = host;
                    } else {
                        API_HOST = "https://" + host;
                    }
                }
            } catch (ParseException e) {
                System.out.println(e.getMessage());
                formatter.printHelp("utility-name", options);
                System.exit(1);
            }
        }
        AppUpload tl = new AppUpload();
        tl.upload(account, passwd, ipaPath);
    }
}
