package com.book.BookProject.order;

import com.book.BookProject.user.UserEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "ORDERS")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    @Column(nullable = false, unique = true, length = 100)
    private String merchantUid;  // Long → String으로 변경

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBER_ID", nullable = false)
    private UserEntity member;

    @Column(nullable = false)
    private LocalDateTime orderDate;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(nullable = false)
    private Double totalAmount;

    @Column(nullable = false, length = 200)
    private String shippingAddress;

    @Column(nullable = false, length = 200)
    private String detailAddress;

    @Column(nullable = false, length = 100)
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String recipientPhone;

    @Column(nullable = false, length = 50)
    private String paymentMethod;

    @Column(nullable = false, length = 50)
    private String paymentStatus;

    // 추가된 필드들
    @Column(nullable = false, length = 200)
    private String bookTitle;  // 책 제목

    @Column(nullable = false, length = 100)
    private String bookAuthor;  // 책 저자

    @Column(nullable = false, length = 100)
    private String bookPublisher;  // 책 출판사

    @Column(nullable = false, length = 500)  // URL 필드 길이를 500자로 확장
    private String bookImageUrl;  // 책 이미지 URL

    @Builder.Default
    @Column(updatable = false)
    private LocalDateTime createDate = LocalDateTime.now();

    @Builder.Default
    private LocalDateTime updateDate = LocalDateTime.now();

    @Transient
    private String formattedTotalAmount; // 포맷팅된 가격을 저장할 임시 필드
}