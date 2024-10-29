package com.book.BookProject.oauth2;

import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;


@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final HttpSession session;

    public CustomOAuth2UserService(UserRepository userRepository, HttpSession session) {
        this.userRepository = userRepository;
        this.session = session;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String userNameAttributeName = userRequest.getClientRegistration().getProviderDetails()
                .getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, userNameAttributeName, oAuth2User.getAttributes());

        UserEntity userEntity = saveOrUpdate(attributes);

        // 닉네임이 없는 경우 회원가입 페이지로 이동
        if (userEntity.getNick() == null || userEntity.getNick().isEmpty()) {
            session.setAttribute("socialUser", userEntity);
            session.setAttribute("redirectUri", "/guest/SocialSignup");
        } else {
            // 닉네임이 있는 경우 메인 페이지로 리다이렉트
            session.setAttribute("redirectUri", "/");
        }

        return new CustomOAuth2User(userEntity, oAuth2User.getAttributes());
    }

    private UserEntity saveOrUpdate(OAuthAttributes attributes) {
        return userRepository.findBySocialId(attributes.getSocialId())
                .map(user -> {
                    user.updateSocialEmail(attributes.getEmail());
                    user.setNewUser(false);  // 기존 유저
                    return userRepository.save(user);
                })
                .orElseGet(() -> {
                    UserEntity newUser = attributes.toEntity();
                    newUser.setSocialEmail(attributes.getEmail());
                    newUser.setNewUser(true); // 새로운 유저
                    return userRepository.save(newUser);
                });
    }
}