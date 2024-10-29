package com.book.BookProject.sms;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class VerificationCodeService {

    private final Map<String, String> verificationCodes = new ConcurrentHashMap<>();

    public void saveCode(String username, String code) {
        verificationCodes.put(username, code);
    }

    public String getCode(String username) {
        return verificationCodes.get(username);
    }

    public void removeCode(String username) {
        verificationCodes.remove(username);
    }
}