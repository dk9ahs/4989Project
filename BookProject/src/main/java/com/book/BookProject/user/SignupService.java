package com.book.BookProject.user;


public interface SignupService {
    void registerUser(UserDTO userDTO);  // 일반 회원가입
    boolean isIdUnique(String id);       // ID 중복 체크
    boolean isNickUnique(String nick);   // 닉네임 중복 체크

}