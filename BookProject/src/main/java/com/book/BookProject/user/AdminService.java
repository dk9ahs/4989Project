package com.book.BookProject.user;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final UserRepository userRepository;

    public AdminService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void lockAccount(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원이 없습니다: " + id));
        user.setAccountLocked(1);  // 계정 잠금
        userRepository.save(user);
    }

    @Transactional
    public void unlockAccount(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원이 없습니다: " + id));
        user.setAccountLocked(0);  // 계정 잠금 해제
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(String id) {
        userRepository.deleteById(id);  // 회원 삭제
    }
}