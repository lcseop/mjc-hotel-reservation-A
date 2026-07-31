package com.mjc.hotel.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

	private static final String TOKEN_TYPE_CLAIM = "tokenType";
	private static final String ACCESS_TOKEN_TYPE = "access";
	private static final String REFRESH_TOKEN_TYPE = "refresh";

	private static final String SECRET_KEY = "secret-my-secretkey-hello-secret-my-secretkey-hello";
	private static final long ACCESS_TOKEN_TIME = 1000L * 60 * 60;
	private static final long REFRESH_TOKEN_TIME = 1000L * 60 * 60 * 24 * 14;

	public String createAccessToken(String name) {
		return createToken(name, ACCESS_TOKEN_TYPE, ACCESS_TOKEN_TIME);
	}

	public String createRefreshToken(String name) {
		return createToken(name, REFRESH_TOKEN_TYPE, REFRESH_TOKEN_TIME);
	}

	private String createToken(String name, String tokenType, long expirationMillis) {
		Date now = new Date();

		String token = Jwts.builder()
						.setSubject(name)
						.claim(TOKEN_TYPE_CLAIM, tokenType)
						.setIssuedAt(now)
						.setExpiration(new Date(now.getTime() + expirationMillis))
						.signWith(
										Keys.hmacShaKeyFor(SECRET_KEY.getBytes()),
										SignatureAlgorithm.HS256
						)
						.compact();

		return token;
	}

	public String getName(String token) {
		return getClaims(token).getSubject();
	}

	public boolean validateToken(String token) {
		return validateToken(token, null);
	}

	public boolean validateAccessToken(String token) {
		return validateToken(token, ACCESS_TOKEN_TYPE);
	}

	public boolean validateRefreshToken(String token) {
		return validateToken(token, REFRESH_TOKEN_TYPE);
	}

	public long getAccessTokenExpiresInSeconds() {
		return ACCESS_TOKEN_TIME / 1000;
	}

	public long getRefreshTokenExpiresInSeconds() {
		return REFRESH_TOKEN_TIME / 1000;
	}

	private boolean validateToken(String token, String expectedTokenType) {
		try {
			Claims claims = getClaims(token);
			if (expectedTokenType == null) {
				return true;
			}

			return expectedTokenType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class));
		} catch (Exception e) {
			return false;
		}
	}

	private Claims getClaims(String token) {
		return Jwts.parserBuilder()
						.setSigningKey(
										Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
						)
						.build()
						.parseClaimsJws(token)
						.getBody();
	}

}
