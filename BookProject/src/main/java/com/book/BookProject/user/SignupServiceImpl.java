package com.book.BookProject.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SignupServiceImpl implements SignupService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SignupServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // 일반 회원가입 처리
    @Override
    @Transactional
    public void registerUser(UserDTO userDTO) {
        // ID 중복 체크
        if (!isIdUnique(userDTO.getId())) {
            throw new IllegalArgumentException("이미 사용중인 아이디입니다.");
        }

        // 닉네임 중복 체크
        if (!isNickUnique(userDTO.getNick())) {
            throw new IllegalArgumentException("이미 사용중인 닉네임입니다.");
        }

        // 비밀번호 확인
        if (!userDTO.getPwd().equals(userDTO.getPwdconfirm())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // 권한 설정 (ROLE_USER 기본값)
        String authority = "ROLE_USER";
        if (userDTO.getId().equalsIgnoreCase("admin")) {
            authority = "ROLE_ADMIN";
        }

        // UserEntity 빌드
        UserEntity userEntity = UserEntity.builder()
                .id(userDTO.getId())
                .nick(userDTO.getNick())
                .pwd(passwordEncoder.encode(userDTO.getPwd()))  // 비밀번호 암호화
                .name(userDTO.getName())
                .phone(userDTO.getPhone())
                .email(userDTO.getEmail())
                .zipcode(userDTO.getZipcode())
                .address(userDTO.getAddress())
                .detailAddress(userDTO.getDetailAddress())
                .authority(authority)
                .enabled(1)
                .build();

        // 사용자 정보 DB에 저장
        userRepository.save(userEntity);
    }

    @Override
    public boolean isIdUnique(String id) {
        return !userRepository.existsById(id);
    }

    @Override
    public boolean isNickUnique(String nick) {
        return !userRepository.existsByNick(nick);
    }
}