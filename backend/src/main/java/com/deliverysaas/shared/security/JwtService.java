package com.deliverysaas.shared.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import org.springframework.stereotype.Service;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {
        private final JwtProperties props;
        private final SecretKey key;

        public JwtService(JwtProperties props) {
            this.props = props;
            this.key = Keys.hmacShaKeyFor(props.secret().getBytes(StandardCharsets.UTF_8));
        }

        public String generate(User user) {
            Instant now = Instant.now();
            return Jwts.builder()
                .subject(user.getId().toString())
                .claim("organizationId", user.getOrganization().getId().toString())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from( now.plus(props.expiration())))
                .signWith(key)
                .compact();
        }

        public AuthPrincipal parse(String token) {
            Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return new AuthPrincipal(
                UUID.fromString(claims.getSubject()),
                UUID.fromString(claims.get("organizationId", String.class)),
                UserRole.valueOf(claims.get("role", String.class))
            );
        }       
}