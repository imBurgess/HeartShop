package com.HeartShop.util;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

/**
 * JWT Token 工具類
 */
@Component
public class JwtUtil {

    private final Key key;
    private final long expiration;

    public JwtUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration
    ) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiration = expiration;
    }

    /**
     * 生成 JWT Token
     *
     * @param memberId 會員 ID
     * @param email    會員 Email
     * @param role     會員角色
     * @return JWT Token 字串
     */
    public String generateToken(Long memberId, String email, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setSubject(String.valueOf(memberId))
                .claim("email", email)
                .claim("role", role)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    /**
     * 從 Token 中解析會員 ID
     *
     * @param token JWT Token（可包含 "Bearer " 前綴）
     * @return 會員 ID
     */
    public Long getMemberIdFromToken(String token) {
        // 移除 "Bearer " 前綴（如果有）
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        String subject = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();

        return Long.valueOf(subject);
    }
}
