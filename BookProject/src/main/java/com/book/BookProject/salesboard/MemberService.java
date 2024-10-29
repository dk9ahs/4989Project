package com.book.BookProject.salesboard;

import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MemberService {
    // 아이디를 통해 닉네임 찾기
    private final UserRepository userRepository;

    public MemberService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public String findNickById(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        return userEntity.getNick();
    }

    // UserEntity 반환 메서드
    public UserEntity findUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));
    }

}
