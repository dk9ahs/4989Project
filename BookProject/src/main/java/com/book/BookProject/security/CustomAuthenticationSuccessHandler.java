package com.book.BookProject.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserServiceImpl userService;

    public CustomAuthenticationSuccessHandler(UserServiceImpl userService) {
        this.userService = userService;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();  // 로그인 성공한 ID

        // 로그인 성공한 경우 실패 시도 초기화 및 lastLoginDate 업데이트
        if (username != null) {
            userService.resetFailedAttempts(username);
            userService.updateLastLoginDate(username);  // 마지막 로그인 시간 업데이트
        }

        // 세션에서 redirectUrl 가져오기
        String redirectUrl = (String) request.getSession().getAttribute("redirectUrl");

        // Referer 확인
        String referer = request.getHeader("Referer");

        if (redirectUrl != null && !redirectUrl.isEmpty()) {
            if (redirectUrl.contains("/signup")) {  // redirectUrl이 "/signup" 경로를 포함하는지 체크
                response.sendRedirect("/");  // 기본 경로로 리다이렉트
            } else {
                request.getSession().removeAttribute("redirectUrl");
                response.sendRedirect(redirectUrl);  // 세션에 저장된 URL로 리다이렉트
            }
        } else if (referer != null && !referer.contains("/login")) {
            response.sendRedirect(referer);  // Referer가 로그인 페이지가 아니면 Referer로 리다이렉트
        } else {
            response.sendRedirect("/");  // 기본 경로로 리다이렉트
        }
    }
}