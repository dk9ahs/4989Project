package com.book.BookProject;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class LoginController {
    @GetMapping("/login")
    public String showLoginPage(HttpServletRequest request, Model model) {
        // 로그인 페이지로 접근하기 전에 이전 페이지 URL을 세션에 저장
        String referer = request.getHeader("Referer");
        if (referer != null && !referer.contains("/login")) {
            request.getSession().setAttribute("redirectUrl", referer);
        }
        return "guest/Login";
    }
    @GetMapping("/guest/unlock")
    public String showUnlockUserPage() {
        return "guest/UnlockUser";  // UnlockUser.html 반환
    }
}