package com.ericsson.eiffel.remrem.publish.config;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(
            CustomAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {
        if (authException instanceof BadCredentialsException) {
            LOGGER.warn("Bad Credentials {}", HttpStatus.UNAUTHORIZED);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid credentials");
        }
        else if (authException instanceof InternalAuthenticationServiceException) {
            Throwable cause = authException.getCause();
            if (cause instanceof org.springframework.ldap.CommunicationException communicationException) {
                cause = communicationException.getCause();
                if (cause instanceof javax.naming.CommunicationException namingCommunicationException) {
                    String message = namingCommunicationException.toString();
                    LOGGER.warn("Communication problem: {}; {}", HttpStatus.GATEWAY_TIMEOUT, message);
                    response.sendError(HttpServletResponse.SC_GATEWAY_TIMEOUT, message);
                    response.flushBuffer();
                }
            }
        }
    }
}
