package com.uetty.common.tool.core;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.Map;

@SuppressWarnings("unused")
@Slf4j
public class JwtUtil {

    private static final String KEY_USERNAME = "username";
    private static final String ISSUER = "my-team-name";

    private static String createToken0(String secret, Long expire, Date notBefore, String username, Map<String, String> data) {
        if (data == null) {
            data = new HashMap<>();
        }
        if (username != null) {
            data.put(KEY_USERNAME, username);
        }

        JWTCreator.Builder builder = JWT.create();
        data.forEach(builder::withClaim);
        builder.withIssuer(ISSUER);
        Date date = new Date();
        builder.withIssuedAt(date);
        if (expire != null) {
            Date expireDate = new Date(date.getTime() + expire);
            builder.withExpiresAt(expireDate);
        }
        if (notBefore != null) {
            builder.withNotBefore(notBefore);
        }
        return builder.sign(Algorithm.HMAC256(secret));
    }

    public static String createToken(String secret, long expire, Map<String, String> data) {
        return createToken0(secret, expire, null,null, data);
    }

    public static String createToken(String secret, String username, Map<String, String> data) {
        return createToken0(secret, null, null, username, data);
    }

    public static String createToken(String secret, long expire, String username, Map<String, String> data) {
        return createToken0(secret, expire, null, username, data);
    }

    public static String createToken(String secret, long expire, Date notBefore, String username, Map<String, String> data) {
        return createToken0(secret, expire, notBefore, username, data);
    }

    public static String createToken(String secret, Map<String, String> data) {
        return createToken0(secret, null, null,null, data);
    }

    public static void verify(String jwtToken, String secret) {
//        if (jwtToken == null) {
//            jwtToken = "..";
//        }
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret)).build();
        verifier.verify(jwtToken);
    }

    public static Map<String, String> getData(String jwtToken) {
        DecodedJWT jwt = JWT.decode(jwtToken);
        Map<String, Claim> claims = jwt.getClaims();
        Map<String, String> map = new HashMap<>();

        for (Map.Entry<String, Claim> entry : claims.entrySet()) {
            switch (entry.getKey()) {
                case "iat":
                case "exp":
                    break;
                default:
                    map.put(entry.getKey(), entry.getValue().asString());
            }
        }
        return map;
    }

    @SuppressWarnings("unused")
    public static String getUsername(String jwtToken) {
        DecodedJWT jwt = JWT.decode(jwtToken);
        return jwt.getClaims().get(KEY_USERNAME).asString();
    }
}
