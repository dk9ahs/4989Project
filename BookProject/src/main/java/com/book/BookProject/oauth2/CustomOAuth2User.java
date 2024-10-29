package com.book.BookProject.oauth2;

import com.book.BookProject.user.UserEntity;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

@Getter
public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final UserEntity userEntity;
    private final Map<String, Object> attributes;
    private String redirectUri;

    public CustomOAuth2User(UserEntity userEntity, Map<String, Object> attributes, String redirectUri) {
        this.userEntity = userEntity;
        this.attributes = attributes;
        this.redirectUri = redirectUri;
    }

    public CustomOAuth2User(UserEntity userEntity, Map<String, Object> attributes) {
        this.userEntity = userEntity;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    public String getName() {
        return String.valueOf(userEntity.getIdx());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(userEntity.getAuthority()));
    }

    @Override
    public String getPassword() {
        return userEntity.getPwd();
    }

    @Override
    public String getUsername() {
        return userEntity.getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return userEntity.getAccountLocked() == 0;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return userEntity.getEnabled() == 1;
    }
}