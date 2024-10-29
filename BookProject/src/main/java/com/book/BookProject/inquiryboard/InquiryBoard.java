package com.book.BookProject.inquiryboard;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDate;
import java.time.LocalDateTime;

@DynamicInsert
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Entity
@Table(name = "INQUIRYBOARD")
public class InquiryBoard {

    @Id // 엔티티의 주키(primary key)
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 자동증가(Auto Increment)를 지정
    private  Long qidx; // PK, 인덱스
    @Column(name = "ORIGINNO")
    private Long origin; // 그룹 번호 (원글 번호)
    @Column(name = "GROUPORD")
    private  int group; // 그룹 내 순서
    @Column(name = "GROUPLAYER")
    private int layer; // 그룹 계층 (답글의 깊이)
    @Column(name="QRESPONSES")
    private int responses; // 답변 여부
    @Column(name="NICK")
    private String nick; // 작성자 - 닉네임
    @Column(name="QTITLE")
    private String title; // 제목
    @Column(name="QCONTENT")
    private String content; // 내용
    @Column(name="QPASS")
    private String pass; // 글 비밀번호
    @CreationTimestamp
    @Column(name="QCREATE_DATE")
    private LocalDateTime createDate; // 작성일
    @UpdateTimestamp
    @Column(name="QUPDATE_DATE")
    private LocalDateTime updateDate; // 수정일
    @Column(name="QVIEW_COUNT")
    private int viewCount; // 조회수
    @Column(name="QOFILE")
    private String ofile; // 원본 파일명
    @Column(name="QSFILE")
    private String sfile; // 저장된 파일명
}
