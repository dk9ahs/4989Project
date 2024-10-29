package com.book.BookProject.sms;

import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/sms")
public class SmsController {

    private final CoolSmsService coolSmsService;
    private final VerificationCodeService verificationCodeService;

    @Autowired
    public SmsController(CoolSmsService coolSmsService, VerificationCodeService verificationCodeService) {
        this.coolSmsService = coolSmsService;
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/send")
    public String sendSms(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String phoneNumber = body.get("phoneNumber");
        try {
            String generatedCode = coolSmsService.sendSms(phoneNumber);
            verificationCodeService.saveCode(username, generatedCode);
            return "Generated verification code: " + generatedCode;
        } catch (CoolsmsException e) {
            e.printStackTrace();
            return "Failed to send SMS: " + e.getMessage();
        }
    }
}
