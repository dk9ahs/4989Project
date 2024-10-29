//package com.book.BookProject.oauth2;
//
//import com.book.BookProject.user.UserDTO;
//import com.book.BookProject.user.UserEntity;
//import com.book.BookProject.user.UserRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.UUID;
//
//@Service
//public class SocialSignupService {
//
//    private final UserRepository userRepository;
//
//    public SocialSignupService(UserRepository userRepository) {
//        this.userRepository = userRepository;
//    }
//
//    // 소셜 회원가입 처리
//    @Transactional
//    public void registerSocialUser(UserDTO userDTO) {
//        // 소셜 회원가입은 ID 중복 체크 없이 처리
//        if (!isNickUnique(userDTO.getNick())) {
//            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
//        }
//
//        String authority = "ROLE_USER";
//        if (userDTO.getId() != null && userDTO.getId().equalsIgnoreCase("admin")) {
//            authority = "ROLE_ADMIN";
//        }
//
//        // 소셜 로그인 시에는 비밀번호를 "SOCIAL_LOGIN"으로 설정
//        UserEntity userEntity = UserEntity.builder()
//                .id(userDTO.getId() == null ? UUID.randomUUID().toString() : userDTO.getId())  // 소셜 ID 또는 임의 ID 설정
//                .nick(userDTO.getNick())  // 사용자 입력한 닉네임
//                .pwd("SOCIAL_LOGIN")  // 비밀번호 대신 "SOCIAL_LOGIN" 저장
//                .name(userDTO.getName())  // 사용자 입력한 이름
//                .phone(userDTO.getPhone())  // 사용자 입력한 전화번호
//                .email(userDTO.getEmail())  // 사용자 입력한 이메일
//                .socialId(userDTO.getSocialId())  // 소셜 ID
//                .socialProvider(userDTO.getSocialProvider())  // 소셜 제공자
//                .authority(authority)  // 권한 설정
//                .enabled(1)  // 활성화
//                .build();
//
//        userRepository.save(userEntity);  // DB에 저장
//    }
//
//    public boolean isNickUnique(String nick) {
//        return !userRepository.existsByNick(nick);
//    }
//}