package com.book.BookProject.user;


public interface LoginService {
    boolean validateUser(String id, String password);
    UserEntity getUserById(String id);
}
