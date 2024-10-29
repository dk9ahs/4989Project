package com.book.BookProject.oauth2;

import com.book.BookProject.user.UserEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;
import java.util.UUID;

@Getter
@Builder  // 클래스 레벨에 @Builder 추가
public class OAuthAttributes {

    private Map<String, Object> attributes;
    private String nameAttributeKey;
    private String email;
    private String socialId;
    private String socialProvider;

    public static OAuthAttributes of(String registrationId, String userNameAttributeName, Map<String, Object> attributes) {
        if ("naver".equals(registrationId)) {
            return ofNaver("id", attributes);
        } else if ("kakao".equals(registrationId)) {
            return ofKakao("id", attributes);
        }
        return ofGoogle(userNameAttributeName, attributes);
    }

    private static OAuthAttributes ofGoogle(String userNameAttributeName, Map<String, Object> attributes) {
        return OAuthAttributes.builder()
                .socialId((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .socialProvider("google")
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofNaver(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        return OAuthAttributes.builder()
                .socialId((String) response.get("id"))
                .email((String) response.get("email"))
                .socialProvider("naver")
                .attributes(response)  // 전체 response를 attributes로 설정
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    private static OAuthAttributes ofKakao(String userNameAttributeName, Map<String, Object> attributes) {
        Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");


        return OAuthAttributes.builder()
                .socialId(attributes.get("id").toString())
                .email((String) kakaoAccount.get("email"))
                .socialProvider("kakao")
                .attributes(attributes)
                .nameAttributeKey(userNameAttributeName)
                .build();
    }

    public UserEntity toEntity() {
        return UserEntity.builder()
                .id(UUID.randomUUID().toString())
                .socialId(socialId)
                .socialProvider(socialProvider)
                .email(email)
                .pwd("SOCIAL_LOGIN")  // 소셜 로그인 사용자는 비밀번호를 사용하지 않으므로 기본값 설정
                .enabled(1)
                .build();
    }
}