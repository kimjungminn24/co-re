package com.core.api.filter;


import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class LoggingFilter implements Filter {


    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        if(servletRequest instanceof HttpServletRequest httpServletRequest){
            String url = httpServletRequest.getRequestURI();
            String method = httpServletRequest.getMethod();
            log.trace("들어오는 요청 : URL={}, Method={}",url,method);
        }

        filterChain.doFilter(servletRequest,servletResponse);
    }
}
