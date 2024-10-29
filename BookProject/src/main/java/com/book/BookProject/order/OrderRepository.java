package com.book.BookProject.order;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 사용자별 주문 조회 메서드
    List<Order> findByMemberId(String memberId);

}