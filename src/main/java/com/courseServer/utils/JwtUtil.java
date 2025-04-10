package com.courseServer.utils;

import java.util.Date;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JwtUtil {

	private static final String SECRET_KEY = "you_cant_guess_MY_Key";
	private static final long EXPIRATION_TIME = 3600000; // 1 hour in milliseconds

	public static String createToken(String name) {
		return JWT.create()
				.withSubject(name)
				.withIssuer("courseServer")
				.withIssuedAt(new Date())
				.withExpiresAt(new Date(System.currentTimeMillis()+EXPIRATION_TIME))
				.sign(Algorithm.HMAC256(SECRET_KEY));
	}
	
	public static String validateToken(String token) {
		try{JWTVerifier vertifier = JWT.require(Algorithm.HMAC256(SECRET_KEY))
				.withIssuer("courseServer")
				.build();
		DecodedJWT jwt = vertifier.verify(token);
		return jwt.getSubject();
		} catch (JWTVerificationException e) {
			return null;
		}
	}
}
