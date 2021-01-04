package com.github.coerx.qarchiver.common.util;

import java.util.Map;

public class MapUtil {
    public static String mapToUrlEncodedString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    StringUtil.urlEncodeUTF8(entry.getKey().toString()),
                    StringUtil.urlEncodeUTF8(entry.getValue().toString())
            ));
        }
        return sb.toString();
    }

    public static String mapToUrlQueryString(Map<?, ?> map) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            if (sb.length() > 0) {
                sb.append("&");
            }
            sb.append(String.format("%s=%s",
                    entry.getKey().toString(),
                    entry.getValue().toString()));
        }
        return sb.toString();
    }
}
