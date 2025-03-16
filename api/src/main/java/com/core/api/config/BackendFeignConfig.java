package com.core.api.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class BackendFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            System.out.println(requestTemplate.url());
            if (!requestTemplate.url()
                    .equals("/users/search/git-token")) {
                String token = getAccessToken();
                requestTemplate.header("Authorization", token);
            }
        };
    }

    private String getAccessToken() throws AccessDeniedException {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return authentication.getPrincipal()
                    .toString();
        }
        throw new AccessDeniedException("Access token is missing from SecurityContext");
    }


}
