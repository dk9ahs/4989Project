package com.book.BookProject;

import com.book.BookProject.user.FindService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/find")
public class FindIdPasswordController {

    private final FindService findService;

    public FindIdPasswordController(FindService findService) {
        this.findService = findService;
    }

    // 아이디/비밀번호 찾기 폼 페이지 통합
    @GetMapping
    public String showFindIdPasswordForm() {
        return "guest/FindIdPwd"; // 아이디 및 비밀번호 찾기 통합 페이지
    }

    // 아이디 찾기 요청 처리 (전화번호로)
    @PostMapping("/id")
    @ResponseBody
    public Map<String, Object> findId(@RequestBody Map<String, String> requestData) {
        String name = requestData.get("name");
        String phone = requestData.get("phone");

        Map<String, Object> response = new HashMap<>();
        try {
            String foundId = findService.findIdByNameAndPhone(name, phone);
            response.put("foundId", foundId); // 찾은 아이디를 반환
        } catch (IllegalArgumentException e) {
            response.put("foundId", null); // 일치하는 정보를 찾을 수 없음
        }

        return response; // JSON 형식으로 응답
    }

    // 인증번호 전송
    @PostMapping("/sendAuthCode")
    @ResponseBody
    public Map<String, Boolean> sendAuthCode(@RequestBody Map<String, String> request) {
        String name = request.get("name");
        String email = request.get("email");
        String id = request.get("id");

        Map<String, Boolean> response = new HashMap<>();
        if (findService.isUserValid(name, email, id)) {
            findService.sendVerificationCode(email);
            response.put("success", true);
        } else {
            response.put("success", false);
        }
        return response;
    }

    // 인증번호 확인 및 임시 비밀번호 발급
    @PostMapping("/verifyAuthCode")
    @ResponseBody
    public Map<String, Object> verifyAuthCode(@RequestBody Map<String, String> request) {
        String authCode = request.get("authCode");
        String email = request.get("email");

        Map<String, Object> response = new HashMap<>();
        if (findService.verifyAuthCode(authCode)) {
            String tempPassword = findService.generateTempPasswordAndSendEmail(email); // 임시 비밀번호 생성 및 전송
            response.put("verified", true);
            response.put("tempPassword", tempPassword);
        } else {
            response.put("verified", false);
        }
        return response;
    }
}