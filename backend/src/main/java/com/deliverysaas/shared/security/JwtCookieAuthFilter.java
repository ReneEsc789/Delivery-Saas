package com.deliverysaas.shared.security;

import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import com.deliverysaas.organizations.domain.OrganizationStatus;
import com.deliverysaas.users.UserRepository;
import com.deliverysaas.users.domain.User;
import com.deliverysaas.users.domain.UserStatus;
import org.springframework.web.filter.OncePerRequestFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtCookieAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthProperties authProperties;
    private final UserRepository userRepository;

    public JwtCookieAuthFilter(JwtService jwtService, AuthProperties authProperties, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.authProperties = authProperties;
        this.userRepository = userRepository;
    }

    private String extractToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (cookie.getName().equals(authProperties.cookieName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            AuthPrincipal tokenPrincipal = jwtService.parse(token);
            User user = userRepository.findByIdAndOrganizationId(tokenPrincipal.userId(), tokenPrincipal.organizationId())
                .filter(found -> found.getStatus() == UserStatus.ACTIVE)
                .filter(found -> found.getOrganization().getStatus() == OrganizationStatus.ACTIVE)
                .orElseThrow();
            AuthPrincipal principal = new AuthPrincipal(user.getId(), user.getOrganization().getId(), user.getRole());

            List<SimpleGrantedAuthority> authorities =
                List.of(new SimpleGrantedAuthority("ROLE_" + principal.role().name()));

            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, authorities);

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }
}
