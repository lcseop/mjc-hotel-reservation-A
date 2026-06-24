package com.mjc.hotel.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtProvider {

	private final String SECRET_KEY = "secret-my-secretkey-hello-secret-my-secretkey-hello";
	private final long ACCESS_TOKEN_TIME = 1000 * 60 * 30;

	public String createToken(String name) {
		Date now = new Date();

		String token = Jwts.builder()
						.setSubject(name)
						.setIssuedAt(now)
						.setExpiration(new Date(now.getTime() + ACCESS_TOKEN_TIME))
						.signWith(
										Keys.hmacShaKeyFor(SECRET_KEY.getBytes()),
										SignatureAlgorithm.HS256
						)
						.compact();

		return token;
	}

	public String getName(String token) {
		return Jwts.parserBuilder()
						.setSigningKey(
										Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
						)
						.build()
						.parseClaimsJws(token)
						.getBody()
						.getSubject();
	}

	public boolean validateToken(String token) {
		try {
			Jwts.parserBuilder()
							.setSigningKey(
											Keys.hmacShaKeyFor(SECRET_KEY.getBytes())
							)
							.build()
							.parseClaimsJws(token);

			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
