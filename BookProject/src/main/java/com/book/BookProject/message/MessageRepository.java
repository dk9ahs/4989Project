package com.book.BookProject.message;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    // 받은 쪽지
    Page<Message> findByReceiverContainingAndViewstatus(String receiver, int viewstatus, Pageable pageable);

    // 보낸 쪽지
    Page<Message> findBySenderContainingAndSendviewstatus(String sender, int sendviewstatus, Pageable pageable);

    // 내게 쓴 쪽지 리스트
    @Query("SELECT m FROM MESSAGE m WHERE (m.receiver LIKE %:receiver% OR m.sender = m.receiver) AND m.receiver LIKE %:receiver%")
    Page<Message> findByReceiverOrSender(String receiver, Pageable pageable);
    
    // 안읽은 쪽지 카운트
    long countByReceiverAndReadstatusAndViewstatus(String receiver, int readstatus, int viewstatus);

    // 쪽지 읽음으로 표시
    @Modifying
    @Query(value = "update MESSAGE set readstatus = 1 where msidx=:msidx", nativeQuery = true)
    int updateReadstatus(@Param("msidx") Long msidx);

    // 받은쪽지 list에서 삭제
    @Modifying
    @Query(value = "update MESSAGE set viewstatus = 0 where msidx=:msidx", nativeQuery = true)
    int updateViewstatus(@Param("msidx") Long msidx);

    // 보낸 쪽지 list에서 삭제
    @Modifying
    @Query(value = "update MESSAGE set sendviewstatus = 0 where msidx=:msidx", nativeQuery = true)
    int updateSendViewstatus(@Param("msidx") Long msidx);

}
