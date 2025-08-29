package com.runky.global.security.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.runky.global.error.GlobalErrorCode;
import com.runky.global.response.ApiResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        GlobalErrorCode exception = (GlobalErrorCode) request.getAttribute("exception");
        if (exception == GlobalErrorCode.NOT_LOGIN_MEMBER) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json; charset=UTF-8");

            ApiResponse<Void> apiResponse = ApiResponse.error(GlobalErrorCode.NOT_LOGIN_MEMBER);
            response.getWriter().write(
                    objectMapper.writeValueAsString(apiResponse)
            );
        }
        else if (exception == GlobalErrorCode.EXPIRED_TOKEN) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json; charset=UTF-8");

            ApiResponse<Void> apiResponse = ApiResponse.error(GlobalErrorCode.EXPIRED_TOKEN);
            response.getWriter().write(
                    objectMapper.writeValueAsString(apiResponse)
            );
        }
        else if (exception == GlobalErrorCode.INVALID_TOKEN) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json; charset=UTF-8");

            ApiResponse<Void> apiResponse = ApiResponse.error(GlobalErrorCode.INVALID_TOKEN);
            response.getWriter().write(
                    objectMapper.writeValueAsString(apiResponse)
            );
        }
    }
}
