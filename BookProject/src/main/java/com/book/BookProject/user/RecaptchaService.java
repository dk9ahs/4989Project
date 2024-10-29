package com.book.BookProject.user;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class RecaptchaService {

    @Value("${recaptcha.secret}")
    private String secretKey;

    private static final String RECAPTCHA_VERIFY_URL = "https://www.google.com/recaptcha/api/siteverify";

    public boolean verifyRecaptcha(String recaptchaResponse) {
        String url = RECAPTCHA_VERIFY_URL + "?secret=" + secretKey + "&response=" + recaptchaResponse;

        RestTemplate restTemplate = new RestTemplate();
        RecaptchaResponse response = restTemplate.postForObject(url, null, RecaptchaResponse.class);

        return response != null && response.isSuccess();
    }

    // reCAPTCHA 응답을 처리할 클래스
    public static class RecaptchaResponse {
        private boolean success;
        private String challenge_ts;
        private String hostname;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public String getChallenge_ts() {
            return challenge_ts;
        }

        public void setChallenge_ts(String challenge_ts) {
            this.challenge_ts = challenge_ts;
        }

        public String getHostname() {
            return hostname;
        }

        public void setHostname(String hostname) {
            this.hostname = hostname;
        }
    }
}
