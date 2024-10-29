package com.book.BookProject.sms;


import net.nurigo.java_sdk.api.Message;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Random;

@Service
public class CoolSmsService {

    private static final Logger logger = LoggerFactory.getLogger(CoolSmsService.class);


    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.api.number}")
    private String fromPhoneNumber;

    public String sendSms(String to) throws CoolsmsException {
        try {
            // 랜덤한 4자리 인증번호 생성
            String numStr = generateRandomNumber();

            // 생성된 인증번호를 로그로 출력
            logger.info("Generated verification code: {}", numStr);

            // Message 객체 생성 시 API 키와 시크릿 키를 넘겨줌
            Message coolsms = new Message(apiKey, apiSecret);

            HashMap<String, String> params = new HashMap<>();
            params.put("to", to);    // 수신 전화번호
            params.put("from", fromPhoneNumber);    // 발신 전화번호
            params.put("type", "sms");
            params.put("text", "인증번호는 [" + numStr + "] 입니다.");

            // 메시지 전송
            coolsms.send(params);

            return numStr; // 생성된 인증번호 반환

        } catch (Exception e) {
            // CoolsmsException 생성자를 사용할 때, 적절한 에러 코드를 전달
            throw new CoolsmsException("Failed to send SMS", -1); // -1은 에러 코드로 가정한 값
        }
    }

    // 랜덤한 4자리 숫자 생성 메서드
    private String generateRandomNumber() {
        Random rand = new Random();
        StringBuilder numStr = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            numStr.append(rand.nextInt(10));
        }
        return numStr.toString();
    }
}