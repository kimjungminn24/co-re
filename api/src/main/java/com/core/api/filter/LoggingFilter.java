package com.core.api.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class LoggingFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(servletRequest instanceof HttpServletRequest httpServletRequest){

            ContentCachingRequestWrapper cachingRequest = new ContentCachingRequestWrapper(httpServletRequest);

            String url = cachingRequest.getRequestURI();
            String method = cachingRequest.getMethod();
            String body = cachingRequest.getReader().lines().reduce("",String::concat);

            log.trace("들어오는 요청 : URL={}, Method={} Body={}",url,method,body);
            filterChain.doFilter(cachingRequest,servletResponse);
            return;
        }

       filterChain.doFilter(servletRequest,servletResponse);
    }

}
