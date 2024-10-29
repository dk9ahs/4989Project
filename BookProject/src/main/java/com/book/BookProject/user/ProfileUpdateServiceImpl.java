package com.book.BookProject.user;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileUpdateServiceImpl implements ProfileUpdateService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Logger logger = LoggerFactory.getLogger(ProfileUpdateServiceImpl.class);

    public ProfileUpdateServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        logger.info("조회하려는 username: {}", username);

        UserEntity userEntity;

        // 소셜 로그인 사용자인지 일반 사용자인지 확인하여 조회
        if (username.contains("@")) {
            userEntity = userRepository.findBySocialEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("소셜 로그인 사용자를 찾을 수 없습니다."));
        } else {
            userEntity = userRepository.findById(username)
                    .orElseThrow(() -> new IllegalArgumentException("일반 사용자를 찾을 수 없습니다."));
        }

        return convertToDto(userEntity);
    }

    @Override
    public boolean isSocialUser(String username) {
        return username.contains("@")
                ? userRepository.findBySocialEmail(username)
                .map(user -> user.getSocialProvider() != null)
                .orElse(false)
                : userRepository.findById(username)
                .map(user -> user.getSocialProvider() != null)
                .orElse(false);
    }

    @Override
    @Transactional
    public void updateUserProfile(UserDTO userDTO, String username) {
        UserEntity userEntity = isSocialUser(username)
                ? userRepository.findBySocialEmail(username)
                .orElseThrow(() -> new IllegalArgumentException("소셜 로그인 사용자를 찾을 수 없습니다."))
                : userRepository.findById(username)
                .orElseThrow(() -> new IllegalArgumentException("일반 사용자를 찾을 수 없습니다."));

        // 소셜 로그인 사용자의 비밀번호와 이메일 수정 불가
        if (isSocialUser(username)) {
            if (userDTO.getPwd() != null && !userDTO.getPwd().isEmpty()) {
                throw new IllegalArgumentException("소셜 로그인 사용자는 비밀번호를 수정할 수 없습니다.");
            }
            if (!userDTO.getEmail().equals(userEntity.getEmail())) {
                throw new IllegalArgumentException("소셜 로그인 사용자는 이메일을 수정할 수 없습니다.");
            }
        } else {
            // 일반 사용자: 비밀번호 수정
            if (userDTO.getPwd() != null && !userDTO.getPwd().isEmpty()) {
                if (!passwordEncoder.matches(userDTO.getCurrentPwd(), userEntity.getPwd())) {
                    throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
                }
                if (!userDTO.getPwd().equals(userDTO.getPwdconfirm())) {
                    throw new IllegalArgumentException("새 비밀번호가 일치하지 않습니다.");
                }
                userEntity.setPwd(passwordEncoder.encode(userDTO.getPwd()));
            }
            userEntity.setEmail(userDTO.getEmail());
        }

        // 나머지 정보 업데이트
        userEntity.setNick(userDTO.getNick());
        userEntity.setPhone(userDTO.getPhone());
        userEntity.setZipcode(userDTO.getZipcode());
        userEntity.setAddress(userDTO.getAddress());
        userEntity.setDetailAddress(userDTO.getDetailAddress());

        // 사용자 정보 저장
        userRepository.save(userEntity);
    }

    // Entity -> DTO 변환
    private UserDTO convertToDto(UserEntity userEntity) {
        return UserDTO.builder()
                .id(userEntity.getId())
                .nick(userEntity.getNick())
                .email(userEntity.getEmail())
                .phone(userEntity.getPhone())
                .zipcode(userEntity.getZipcode())
                .address(userEntity.getAddress())
                .detailAddress(userEntity.getDetailAddress())
                .socialProvider(userEntity.getSocialProvider())
                .build();
    }

    @Transactional
    public void deleteUserById(String username) {
        // 소셜 로그인 사용자와 일반 사용자를 구분하여 삭제 처리
        if (isSocialUser(username)) {
            // 소셜 로그인 사용자는 socialEmail로 사용자 정보 삭제
            UserEntity userEntity = userRepository.findBySocialEmail(username)
                    .orElseThrow(() -> new IllegalArgumentException("소셜 로그인 사용자를 찾을 수 없습니다."));
            userRepository.delete(userEntity); // 소셜 사용자 삭제
        } else {
            // 일반 사용자는 id로 삭제
            userRepository.deleteById(username);
        }
    }
}