package com.deliverysaas.shared.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.auth")
public record AuthProperties(String cookieName, boolean cookieSecure) {}
