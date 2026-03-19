package com.cvyuh.service.am;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import java.util.List;

/**
 * Utility for building shard-targeted headers.
 * Strips amlbcookie from incoming headers and sets it for the target shard.
 * "am-0" → amlbcookie=01, "am-1" → amlbcookie=02, "am-2" → amlbcookie=03
 */
public final class AMShardUtils {

    private AMShardUtils() {}

    /**
     * @param shardHost shard host like "am-0.int.cvyuh.local" or shard ID like "am-0"
     */
    public static MultivaluedMap<String, String> shardHeaders(String shardHost, MultivaluedMap<String, String> incomingHeaders) {
        MultivaluedMap<String, String> headers = new MultivaluedHashMap<>(incomingHeaders);

        String shardId = shardHost.replaceAll("^am-(\\d+).*", "$1");
        String cookieValue = String.format("%02d", Integer.parseInt(shardId) + 1);

        List<String> cookies = headers.getOrDefault("Cookie", List.of());
        String rewrittenCookie;
        if (!cookies.isEmpty()) {
            String original = cookies.get(0);
            String cleaned = java.util.Arrays.stream(original.split(";\\s*"))
                    .filter(c -> !c.trim().startsWith("amlbcookie="))
                    .collect(java.util.stream.Collectors.joining("; "));
            rewrittenCookie = cleaned.isEmpty()
                    ? "amlbcookie=" + cookieValue
                    : cleaned + "; amlbcookie=" + cookieValue;
        } else {
            rewrittenCookie = "amlbcookie=" + cookieValue;
        }
        headers.putSingle("Cookie", rewrittenCookie);
        return headers;
    }
}
