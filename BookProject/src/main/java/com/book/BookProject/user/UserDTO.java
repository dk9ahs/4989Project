package com.book.BookProject.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
    private Long idx;               // 사용자 IDX
    private String id;
    private String nick;           // 닉네임
    private String pwd;            // 비밀번호
    private String pwdconfirm;     // 비밀번호 확인
    private String name;           // 이름
    private String phone;          // 전화번호
    private String email;          // 이메일
    private String zipcode;        // 우편번호
    private String address;        // 주소
    private String detailAddress;  // 상세 주소
    private LocalDateTime createDate;  // 생성 날짜
    private LocalDateTime updateDate;  // 수정 날짜
    private String authority;      // 권한 (e.g., ROLE_USER)
    private int enabled;           // 활성화 여부
    private String socialId;       // 소셜 아이디
    private String socialProvider; // 소셜 제공자
    private String socialEmail;    // 소셜 이메일
    private int failedAttempts;    // 실패 시도 횟수
    private int accountLocked;     // 계정 잠금 여부
    private String currentPwd;      // 현재 비밀번호 필드
    private LocalDateTime lastLoginDate;  // 마지막 로그인 날짜

}
