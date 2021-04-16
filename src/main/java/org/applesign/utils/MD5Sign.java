package org.applesign.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;

import java.security.MessageDigest;
import java.util.*;

public class MD5Sign {
	public static String getSign(SortedMap<String, String> sortedMap,String pwd) {
		ArrayList<String> paramList = new ArrayList<>();
		Set<Map.Entry<String, String>> entries = sortedMap.entrySet();
		Iterator<Map.Entry<String, String>> iterator = entries.iterator();
		while (iterator.hasNext()) {
			Map.Entry<String, String> entry = iterator.next();
			paramList.add(entry.getKey() + "=" + entry.getValue());
		}
		String paramString = StringUtils.join(paramList, "&") + "." + pwd;
		// return DigestUtils.md5DigestAsHex(paramString.getBytes());
		return DigestUtils.md5Hex(paramString.getBytes());
	}
}
