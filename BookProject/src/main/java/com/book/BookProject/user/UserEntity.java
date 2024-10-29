package com.book.BookProject.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "MEMBER")  // 테이블 이름을 대문자로 설정
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // 기본 키 자동 생성
    private Long idx;               // 기본 키 (자동 생성)

    @Column(nullable = false, length = 100)  // VARCHAR(100)
    private String id;

    @Column(nullable = false, unique = true, length = 50)  // VARCHAR(50), UNIQUE 제약 조건 추가
    private String nick;           // 고유 닉네임

    @Column(nullable = false, length = 512)  // VARCHAR(512)
    private String pwd;            // 비밀번호

    @Column(nullable = false, length = 100)  // VARCHAR(100)
    private String name;           // 이름

    @Column(nullable = false, length = 20)  // VARCHAR(20)
    private String phone;          // 전화번호

    @Column(nullable = false, unique = true, length = 255)  // VARCHAR(255), UNIQUE 제약 조건 추가
    private String email;          // 이메일

    @Column(length = 100)  // VARCHAR(100)
    private String zipcode;        // 우편번호

    @Column(length = 200)  // VARCHAR(200)
    private String address;        // 주소

    @Column(length = 200)  // VARCHAR(200)
    private String detailAddress;  // 상세 주소

    @Builder.Default
    @Column(updatable = false)   // 생성 시에만 설정, 이후 변경 불가
    private LocalDateTime createDate = LocalDateTime.now();  // 생성 날짜, 기본값 현재 시간

    @Builder.Default
    private LocalDateTime updateDate = LocalDateTime.now();  // 수정 날짜, 기본값 현재 시간

    @Builder.Default
    @Column(length = 20, nullable = false)  // VARCHAR(20)
    private String authority = "ROLE_USER";      // 권한 (기본값 ROLE_USER)

    @Builder.Default
    private int enabled = 1;           // 활성화 여부 (기본값 1)

    @Column(length = 200)  // VARCHAR(100)
    private String socialId;       // 소셜 아이디

    @Column(length = 200)  // VARCHAR(100)
    private String socialProvider; // 소셜 제공자

    @Column(length = 200)  // VARCHAR(100)
    private String socialEmail;    // 소셜 이메일

    @Builder.Default
    private int failedAttempts = 0;    // 실패 시도 횟수, 기본값 0

    @Builder.Default
    private int accountLocked = 0;     // 계정 잠금 여부, 기본값 0

    @Column
    private LocalDateTime lastLoginDate;

    @Transient  // DB에 저장하지 않는 필드
    private boolean isNewUser;

    public boolean isNewUser() {
        return isNewUser;
    }

    public void setNewUser(boolean isNewUser) {
        this.isNewUser = isNewUser;
    }

    public UserEntity updateSocialEmail(String email) {
        this.email = this.socialEmail = email;
        this.updateDate = LocalDateTime.now();
        return this;
    }

}