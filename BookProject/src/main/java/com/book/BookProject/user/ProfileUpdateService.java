package com.book.BookProject.user;

public interface ProfileUpdateService {
    UserDTO getUserByUsername(String username); // 현재 사용자의 정보를 가져오기
    void updateUserProfile(UserDTO userDTO, String username); // 회원 정보 업데이트 처리
    void deleteUserById(String id); // 회원탈퇴

    boolean isSocialUser(String username);

}