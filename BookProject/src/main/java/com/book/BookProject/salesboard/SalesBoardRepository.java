package com.book.BookProject.salesboard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SalesBoardRepository extends JpaRepository<SalesBoard, Long> {

    @Modifying
    @Query(value = "update SALESBOARD set sview_count=sview_count+1 where sidx=:sidx", nativeQuery = true)
    int viewCount(@Param("sidx") Long sidx); // 조회수 증가

    @Modifying
    @Query(value = "update SALESBOARD set slike_count=slike_count+1 where sidx=:sidx", nativeQuery = true)
    int likeCount(@Param("sidx") Long sidx); // 좋아요 증가

    @Modifying
    @Query(value = "update SALESBOARD set slike_count=slike_count-1 where sidx=:sidx", nativeQuery = true)
    int unlikeCount(@Param("sidx") Long sidx); // 좋아요 감소

    @Modifying
    @Query(value = "update SALESBOARD set sdown_count=sdown_count+1 where sidx=:sidx", nativeQuery = true)
    int downCount(@Param("sidx") Long sidx); // 다운로드 수 증가

    Page<SalesBoard> findByTitleContaining(String title, Pageable pageable);

    Page<SalesBoard> findByContentContaining(String content, Pageable pageable);

    Page<SalesBoard> findByNickContaining(String nick, Pageable pageable);

    Page<SalesBoard> findByBooktitleContaining(String booktitle, Pageable pageable);

    Page<SalesBoard> findByAuthorContaining(String author, Pageable pageable);

    Page<SalesBoard> findByPublisherContaining(String publisher, Pageable pageable);
}

