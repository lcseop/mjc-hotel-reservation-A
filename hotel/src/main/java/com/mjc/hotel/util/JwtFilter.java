package com.mjc.hotel.util;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

	private final JwtProvider jwtProvider;

	@Override
	public void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

		String bearerToken = request.getHeader("Authorization");

		if (bearerToken != null && bearerToken.startsWith("Bearer")) {
			String token = bearerToken.substring(7);

			if (jwtProvider.validateAccessToken(token)) {
				String name = jwtProvider.getName(token);

				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(name, null, List.of());
				SecurityContextHolder.getContext().setAuthentication(authentication);

//				UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
//								"tom",
//								null,
//								List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
//				);

			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				response.setContentType("application/json;charset=UTF-8");

				response.getWriter().write("""
					{
					  "success": false,
					  "message": "토큰이 만료되었거나 유효하지 않습니다.",
					  "data": null
					}
				""");

				return;
			}
		}

		filterChain.doFilter(request, response);
	}
}
