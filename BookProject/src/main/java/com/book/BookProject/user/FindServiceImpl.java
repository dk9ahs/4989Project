package com.book.BookProject.user;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class FindServiceImpl implements FindService{
    private final UserRepository userRepository;
    private final JavaMailSender mailSender;
    private String verificationCode;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public FindServiceImpl(UserRepository userRepository, JavaMailSender mailSender,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.mailSender = mailSender;
        this.passwordEncoder = passwordEncoder;
    }

    // 이름과 전화번호로 아이디 찾기 메서드 추가
    @Override
    public String findIdByNameAndPhone(String name, String phone) {
        Optional<UserEntity> userOptional = userRepository.findByNameAndPhone(name, phone);
        if (userOptional.isPresent()) {
            return userOptional.get().getId();
        } else {
            throw new IllegalArgumentException("해당하는 사용자가 없습니다.");
        }
    }

    @Override
    public boolean isUserValid(String name, String email, String id) {
        return userRepository.findByNameAndEmailAndId(name, email, id).isPresent();
    }

    @Override
    public void sendVerificationCode(String email) {
        verificationCode = generateVerificationCode();

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("인증번호 발송");
            helper.setText("인증번호는 " + verificationCode + " 입니다.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("이메일 발송 실패");
        }
    }

    @Override
    public boolean verifyAuthCode(String authCode) {
        return authCode.equals(verificationCode);
    }

    @Override
    public String generateTempPasswordAndSendEmail(String email) {
        String tempPassword = generateTempPassword();

        Optional<UserEntity> userOptional = userRepository.findByEmail(email);
        if (userOptional.isPresent()) {
            UserEntity user = userOptional.get();
            // 임시 비밀번호를 암호화하여 저장
            String encodedTempPassword = passwordEncoder.encode(tempPassword);
            user.setPwd(encodedTempPassword);  // 암호화된 비밀번호로 업데이트
            userRepository.save(user);  // DB 업데이트
            // 임시 비밀번호 전송
            sendTempPasswordEmail(email, tempPassword);
        } else {
            throw new IllegalArgumentException("일치하는 사용자를 찾을 수 없습니다.");
        }

        return tempPassword; // 생성된 임시 비밀번호 반환
    }

    private String generateVerificationCode() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private String generateTempPassword() {
        return UUID.randomUUID().toString().substring(0, 12);  // 임시 비밀번호 생성 (12자)
    }

    private void sendTempPasswordEmail(String email, String tempPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            helper.setTo(email);
            helper.setSubject("임시 비밀번호 발급");
            helper.setText("임시 비밀번호는 " + tempPassword + " 입니다.");
            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("임시 비밀번호 이메일 발송 실패");
        }
    }
}