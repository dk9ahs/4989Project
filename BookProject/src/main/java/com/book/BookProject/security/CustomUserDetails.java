package com.book.BookProject.security;

import com.book.BookProject.user.UserEntity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

public class CustomUserDetails implements UserDetails {
    private final UserEntity userEntity;

    public CustomUserDetails(UserEntity userEntity) {
        this.userEntity = userEntity;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 권한을 설정 (예: "ROLE_USER", "ROLE_ADMIN" 등이 userEntity.getAuthority()에서 반환됨)
        return Collections.singletonList(new SimpleGrantedAuthority(userEntity.getAuthority()));
    }


    @Override
    public String getPassword() {
        return userEntity.getPwd();  // 비밀번호 반환
    }

    @Override
    public String getUsername() {
        return userEntity.getId();  // 사용자 ID 반환
    }

    @Override
    public boolean isEnabled() {
        return userEntity.getEnabled() == 1;  // 계정 활성화 여부 반환
    }

    @Override
    public boolean isAccountNonLocked() {
        return userEntity.getAccountLocked() == 0;  // accountLocked가 0이면 잠기지 않은 상태
    }

    public UserEntity getUserEntity() {
        return userEntity;  // 원본 UserEntity 객체를 반환할 수 있음
    }
}
