package com.book.BookProject.user;

public interface FindService {

    String findIdByNameAndPhone(String name, String phone);  // 전화번호로 아이디 찾기 추가

    // 이름, 이메일, 아이디가 일치하는지 확인
    boolean isUserValid(String name, String email, String id);

    // 인증번호 전송
    void sendVerificationCode(String email);

    // 인증번호 확인
    boolean verifyAuthCode(String authCode);

    // 임시 비밀번호 생성 및 이메일 전송 후 DB에 저장
    String generateTempPasswordAndSendEmail(String email);
}