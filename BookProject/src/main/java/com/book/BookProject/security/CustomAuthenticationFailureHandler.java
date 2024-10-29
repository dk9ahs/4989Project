package com.book.BookProject.security;

import com.book.BookProject.user.UserEntity;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAuthenticationFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final UserServiceImpl userServiceImpl;

    public CustomAuthenticationFailureHandler(UserServiceImpl userServiceImpl) {
        this.userServiceImpl = userServiceImpl;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String username = request.getParameter("username");
        String redirectUrl = "/login?error=true";

        if (exception instanceof LockedException) {
            redirectUrl = "/guest/unlock";  // 계정 잠김일 경우 Unlock 페이지로 리다이렉트
        } else if (exception instanceof InternalAuthenticationServiceException && exception.getCause() instanceof LockedException) {
            // InternalAuthenticationServiceException의 내부에 LockedException이 있는 경우 처리
            redirectUrl = "/guest/unlock";
        } else {
            // 실패 시도 카운트 증가
            try {
                userServiceImpl.increaseFailedAttempts(username);  // 실패 시도 증가
                UserEntity userEntity = userServiceImpl.getUserById(username);
                if (userEntity.getAccountLocked() == 1) {  // 3회 실패로 계정이 잠긴 경우
                    redirectUrl = "/guest/unlock";  // 계정 잠금 후 Unlock 페이지로 리다이렉트
                }
            } catch (Exception e) {
                System.out.println("Failed to increase failed attempts for user: " + username);
            }
        }

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);  // 리다이렉트 처리
    }
}
