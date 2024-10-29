package com.book.BookProject;

import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class MyController {

    @GetMapping("/")
    public String mainPage(HttpSession session, Model model) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 인증 객체 출력 (디버깅 용도)
        System.out.println("Authentication: " + authentication);

        String role = "GUEST"; // 기본 역할은 비회원으로 설정
        if (authentication != null && authentication.isAuthenticated() && !(authentication.getPrincipal().equals("anonymousUser"))) {
            // 권한 정보를 가져옴
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                role = authority.getAuthority(); // 권한을 가져옴 (ROLE_USER 또는 ROLE_ADMIN)
                break; // 여러 개의 권한이 있어도 하나만 가져옴
            }
        }

        model.addAttribute("role", role);

        return "guest/home";  // 메인 페이지로 이동
//        return "admin/Adminpage";  // 메인 페이지로 이동
//        return "guest/home";  // 메인 페이지로 이동
    }
}