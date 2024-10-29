package com.book.BookProject.message;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDTO {
    private Long msidx;
    private String sender;
    private String receiver;
    private String title;
    private String content;
    private LocalDateTime createDate;
    private Integer readstatus;
    private Integer viewstatus;
    private Integer sendviewstatus;
}
