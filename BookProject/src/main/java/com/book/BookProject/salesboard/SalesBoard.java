package com.book.BookProject.salesboard;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicUpdate;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
//모든 필드 값을 파라미터로 받는 생성자를 생성
@AllArgsConstructor
//파라미터가 없는 디폴트 생성자를 생성
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity(name="salesboard")
@DynamicUpdate
public class SalesBoard {

    @Id // 엔티티의 주키(primary key)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sidx;

    private String nick;

    @Column(name = "stitle", nullable = false, columnDefinition = "VARCHAR(100)")
    private String title;

    @Column(name = "sbooktitle", nullable = false, columnDefinition = "VARCHAR(100)")
    private String booktitle;  // 책 제목

    @Column(name = "sauthor", nullable = false, columnDefinition = "VARCHAR(50)")
    private String author;  // 책 지은이

    @Column(name = "spublisher", nullable = false, columnDefinition = "VARCHAR(50)")
    private String publisher;  // 책 출판사

    @Column(name="scontent", nullable = false, columnDefinition = "TEXT")
    private String content;  // 내용

    @Column(name="sclassification", nullable = false, columnDefinition = "VARCHAR(50)")
    private String classification; // 판매, 교환, 나눔

    @Column(name="sregion", nullable = false, columnDefinition = "VARCHAR(50)")
    private String region; // 거래 지역

    @Column(name="sprice", nullable = false, columnDefinition = "Integer default 0")
    private Integer price; // 가격

    @CreationTimestamp
    @Column(name="screate_date", columnDefinition = "DATETIME default CURRENT_TIMESTAMP")
    private LocalDateTime createDate;  // 생성 시간

    @UpdateTimestamp
    @Column(name="supdate_date", columnDefinition = "DATETIME default CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updateDate;  // 수정 시간

    @Column(name="sview_count", columnDefinition = "INT default 0")
    private int viewCount;

    @Column(name="slike_count", columnDefinition = "INT default 0")
    private int likeCount;

    @Column(name="limage", columnDefinition = "VARCHAR(200)")
    private String limage;

    @Column(name="oimage", columnDefinition = "VARCHAR(200)")
    private String oimage;

    @Column(name="simage", columnDefinition = "VARCHAR(200)")
    private String simage;

}