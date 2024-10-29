package com.book.BookProject.oauth2;

import com.book.BookProject.user.UserDTO;
import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class OAuth2Service {

    private final UserRepository userRepository;

    public OAuth2Service(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserDTO getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            String provider = oAuth2User.getAttribute("socialProvider");
            String socialId = getSocialIdFromProvider(oAuth2User, provider);

            UserEntity user = userRepository.findBySocialId(socialId).orElse(null);

            if (user != null) {
                return convertEntityToDTO(user);
            } else {
                // 사용자가 없으면 기본 정보로 UserDTO 생성
                return createDefaultUserDTO(oAuth2User, provider, socialId);
            }
        }

        return null;
    }

    private String getSocialIdFromProvider(OAuth2User oAuth2User, String provider) {
        switch (provider) {
            case "google":
                return oAuth2User.getAttribute("sub");
            case "naver":
                return ((Map<String, Object>) oAuth2User.getAttribute("response")).get("id").toString();
            case "kakao":
                return oAuth2User.getAttribute("id");
            default:
                throw new IllegalArgumentException("Unknown provider: " + provider);
        }
    }

    private UserDTO convertEntityToDTO(UserEntity user) {
        return UserDTO.builder()
                .id(user.getId())
                .nick(user.getNick())
                .email(user.getEmail())
                .name(user.getName())
                .phone(user.getPhone())
                .zipcode(user.getZipcode())
                .address(user.getAddress())
                .detailAddress(user.getDetailAddress())
                .socialId(user.getSocialId())
                .socialProvider(user.getSocialProvider())
                .build();
    }

    private UserDTO createDefaultUserDTO(OAuth2User oAuth2User, String provider, String socialId) {
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        return UserDTO.builder()
                .id(socialId)  // 소셜 ID를 기본으로 설정
                .nick("SOCIAL_" + provider + "_" + socialId)  // 기본 닉네임 설정
                .email(email != null ? email : "default@social.com")  // 이메일이 없으면 기본값 사용
                .name(name != null ? name : "소셜 사용자")  // 이름이 없으면 기본 이름 설정
                .phone("000-0000-0000")  // 기본 전화번호 설정
                .zipcode("00000")  // 기본 우편번호 설정
                .address("Unknown Address")  // 기본 주소 설정
                .detailAddress("Unknown Detail Address")  // 기본 상세주소 설정
                .socialId(socialId)
                .socialProvider(provider)
                .build();
    }
}