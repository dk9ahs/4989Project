package com.book.BookProject.inquiryboard;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class InquiryBoardDTO {
    private Long qidx;         // 인덱스
    private Long origin;        // 그룹 번호 (원글 번호)
    private int group;         // 그룹 내 순서
    private int layer;         // 그룹 계층 (답글의 깊이)
    private int responses;     // 답변여부
    private String nick;       // 작성자 닉네임
    private String title;      // 제목
    private String content;    // 내용
    private String pass;          // 글 비밀번호
    private LocalDateTime createDate; // 작성일
    private LocalDateTime updateDate; // 수정일
    private int viewCount;     // 조회 수
    private String ofile;      // 원본 파일명
    private String sfile;      // 저장된 파일명
}
