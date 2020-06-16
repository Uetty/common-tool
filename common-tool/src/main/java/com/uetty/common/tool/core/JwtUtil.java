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

@Slf4j
public class JwtUtil {

    private static final String KEY_USERNAME = "username";

    public static String createToken(Map<String, String> data, String secret, long expire) {
        JWTCreator.Builder builder = JWT.create();
        if (data != null) {
            data.forEach(builder::withClaim);
        }
        Date date = new Date();
        builder.withIssuedAt(date);
        Date expireDate = new Date(date.getTime() + expire);
        builder.withExpiresAt(expireDate);
        return builder.sign(Algorithm.HMAC256(secret));
    }

    @SuppressWarnings("unused")
    public static String createToken(String username, Map<String, String> data, String secret, long expire) {
        data.put(KEY_USERNAME, username);
        return createToken(data, secret, expire);
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
