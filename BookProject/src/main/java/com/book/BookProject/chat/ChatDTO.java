package com.book.BookProject.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class ChatDTO {
    private String sender;
    private String message;
    private long timestamp;
    private String chatRoomId;
    private Integer viewStatus;
}
