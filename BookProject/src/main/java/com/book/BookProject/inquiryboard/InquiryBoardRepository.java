package com.book.BookProject.inquiryboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface InquiryBoardRepository extends JpaRepository<InquiryBoard, Long>
{
    @Modifying
    @Query(value = "update INQUIRYBOARD set QVIEW_COUNT=QVIEW_COUNT+1 where qidx=:qidx", nativeQuery = true)
    void viewCount(@Param("qidx") Long qidx); // 조회수 증가

    // 제목검색
    Page<InquiryBoard> findByTitleContaining(String title, Pageable pageable);
    // 내용검색
    Page<InquiryBoard> findByContentContaining(String content, Pageable pageable);
    // 작성자검색
    Page<InquiryBoard> findByNickContaining(String nick, Pageable pageable);

    // 게시글 목록 - originNo 기준으로 내림차순, groupOrd 기준으로 오름차순으로 정렬
    @Query(value = "SELECT * FROM InquiryBoard ORDER BY originNo DESC, groupOrd ASC", nativeQuery = true)
    Page<InquiryBoard> findAllOrdered(Pageable pageable); // 게시글 목록 조회

    // 답글 작성 유무 - qidx와 originNo가 같은 경우 qresponses를 1로 변경
    @Modifying
    @Transactional
    @Query(value = "UPDATE INQUIRYBOARD SET qresponses = 1 WHERE qidx = :originNo AND originNo = :originNo", nativeQuery = true)
    void updateResponse(@Param("originNo") Long originNo);
}
