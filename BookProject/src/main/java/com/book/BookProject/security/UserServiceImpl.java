package com.book.BookProject.security;

import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        if (userEntity.getAccountLocked() == 1) {
            throw new LockedException("계정이 잠겼습니다. 관리자에게 문의하세요.");
        }

        return new CustomUserDetails(userEntity);
    }
    // getUserById 메서드 추가
    public UserEntity getUserById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
    }

    // 마지막 로그인 시간 업데이트 메서드
    public void updateLastLoginDate(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        userEntity.setLastLoginDate(LocalDateTime.now());  // 현재 시간으로 업데이트
        userRepository.save(userEntity);  // 변경 사항 저장
    }

    // 실패 시도 초기화 메서드
    public void resetFailedAttempts(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        userEntity.setFailedAttempts(0);  // 실패 시도 초기화
        userRepository.save(userEntity);  // 변경 사항 저장
    }

    // 실패 시도 횟수 증가 메서드
    public void increaseFailedAttempts(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        int newFailedAttempts = userEntity.getFailedAttempts() + 1;
        userEntity.setFailedAttempts(newFailedAttempts);

        if (newFailedAttempts >= 3) {
            userEntity.setAccountLocked(1);  // 3회 실패 시 계정 잠금 설정
        }

        userRepository.save(userEntity);
    }

    public void unlockAccount(String id) {
        UserEntity userEntity = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        userEntity.setAccountLocked(0);  // 계정 잠금 해제
        userEntity.setFailedAttempts(0);  // 실패 시도 횟수 초기화
        userRepository.save(userEntity);  // 변경 사항 저장
    }
}