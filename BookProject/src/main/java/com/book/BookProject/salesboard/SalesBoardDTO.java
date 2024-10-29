package com.book.BookProject.salesboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SalesBoardDTO {

    private Long sidx;  // 게시물 ID
    private String nick;  // 작성자 닉네임
    private String title;
    private String booktitle;   // 책 제목
    private String author;   // 책 지은이
    private String publisher;   // 책 출판사
    private String content;  // 내용
    private String classification; // 판매, 교환, 나눔
    private String region; // 거래 지역
    private Integer price; // 가격
    private LocalDateTime createDate;  // 생성 시간
    private LocalDateTime  updateDate;  // 수정 시간
    private int viewCount; // 조회수
    private int likeCount; // 좋아요수
    private String limage; // 이미지 원본 파일
    private String oimage; // 이미지 원본 파일
    private String simage; // 이미지 저장 파일
}