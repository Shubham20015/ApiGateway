package com.api.Gateway.vaildator;

import com.api.Gateway.exception.UnauthorizedAccessException;
import com.api.Gateway.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

@Component
public class AuthenticationFilter extends AbstractGatewayFilterFactory<AuthenticationFilter.Config> {

    @Autowired
    private RouteValidator validator;

    @Autowired
    private JwtUtil jwtUtil;

    public AuthenticationFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        return ((exchange, chain) -> {
            // Header contains token or not
            if (validator.isSecured.test(exchange.getRequest())) {
                if (!exchange.getRequest().getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                    throw new UnauthorizedAccessException("Missing Authorization header");
                }

                String accessToken = exchange.getRequest().getHeaders().get(HttpHeaders.AUTHORIZATION).getFirst();

                if (accessToken != null && accessToken.startsWith("Bearer ")) {
                    accessToken = accessToken.substring(7);
                }

                try {
                    jwtUtil.validateToken(accessToken);
                } catch (Exception ex) {
                    throw new UnauthorizedAccessException("Invalid Token, Try authorized access to application");
                }
            }

            return chain.filter(exchange);
        });
    }

    public static class Config {}
}
