package com.pd.framework.security.oauth2;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class OAuth2TokenStore {

    private static final Map<String, TokenPair> STORE = new ConcurrentHashMap<>();
    private static final long TTL_MS = 5 * 60 * 1000L;

    private OAuth2TokenStore() {}

    public static class TokenPair {
        private final String token;
        private final String refreshToken;
        private final long createdAt;

        public TokenPair(String token, String refreshToken, long createdAt) {
            this.token = token;
            this.refreshToken = refreshToken;
            this.createdAt = createdAt;
        }

        public String token() { return token; }
        public String refreshToken() { return refreshToken; }
        public long createdAt() { return createdAt; }
    }

    public static String generateCode(String token, String refreshToken) {
        String code = java.util.UUID.randomUUID().toString();
        STORE.put(code, new TokenPair(token, refreshToken, System.currentTimeMillis()));
        return code;
    }

    public static TokenPair exchange(String code) {
        TokenPair pair = STORE.remove(code);
        if (pair == null) return null;
        if (System.currentTimeMillis() - pair.createdAt() > TTL_MS) return null;
        return pair;
    }
}
