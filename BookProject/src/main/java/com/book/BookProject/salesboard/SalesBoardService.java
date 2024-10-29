package com.book.BookProject.salesboard;

import com.book.BookProject.salesboard.Redis.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesBoardService {
    @Autowired
    private SalesBoardRepository salesBoardRepository;
    @Autowired
    private SalesBoardMapper salesBoardMapper;
    @Autowired
    private RedisUtil redisUtil;
    
    // 게시글 보기
    public Page<SalesBoardDTO> getAllSalesBoards(int page, String searchField, String searchWord){
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "sidx"));
        Page<SalesBoard> salesBoardPage;

        if(searchField != null && searchWord != null && !searchWord.isEmpty())
        {
            switch (searchField)
            {
                case "title":
                    salesBoardPage = salesBoardRepository.findByTitleContaining(searchWord, pageable);
                    break;
                case "content":
                    salesBoardPage = salesBoardRepository.findByContentContaining(searchWord, pageable);
                    break;
                case "nick":
                    salesBoardPage = salesBoardRepository.findByNickContaining(searchWord, pageable);
                    break;
                case "booktitle":
                    salesBoardPage = salesBoardRepository.findByBooktitleContaining(searchWord, pageable);
                    break;
                case "author":
                    salesBoardPage = salesBoardRepository.findByAuthorContaining(searchWord, pageable);
                    break;
                case "publisher":
                    salesBoardPage = salesBoardRepository.findByPublisherContaining(searchWord, pageable);
                    break;
                default:
                    salesBoardPage = salesBoardRepository.findAll(pageable);
            }
        }else{
            salesBoardPage = salesBoardRepository.findAll(pageable);
        }

        List<SalesBoardDTO> dtoList = salesBoardMapper.toDtoList(salesBoardPage.getContent());

        return new PageImpl<>(dtoList, salesBoardPage.getPageable(), salesBoardPage.getTotalElements());
    }

    // 게시글 작성하기
    public void createSalesBoard(SalesBoardDTO salesBoardDTO) {
        SalesBoard salesBoard = salesBoardMapper.toEntity(salesBoardDTO);

        salesBoardRepository.save(salesBoard);
    }

    // 게시글 상세 조회
    public SalesBoardDTO getSalesBoardById(Long sidx) {
        SalesBoard salesBoard = salesBoardRepository.findById(sidx).get();

        return salesBoardMapper.toDto(salesBoard);
    }

    // 게시글 삭제하기
    public void deletedSalesBored(Long sidx){
        salesBoardRepository.deleteById(sidx);
    }

    // 게시글 수정하기
    public void updateSalesBored(Long sidx, SalesBoardDTO salesBoardDTO){
        SalesBoard salesBoard = salesBoardRepository.findById(sidx).get();
        SalesBoardDTO originalDTO = salesBoardMapper.toDto(salesBoard);

        originalDTO.setSidx(sidx);
        originalDTO.setNick(salesBoardDTO.getNick());
        originalDTO.setClassification(salesBoardDTO.getClassification());
        originalDTO.setRegion(salesBoardDTO.getRegion());
        originalDTO.setTitle(salesBoardDTO.getTitle());
        originalDTO.setBooktitle(salesBoardDTO.getBooktitle());
        originalDTO.setAuthor(salesBoardDTO.getAuthor());
        originalDTO.setPublisher(salesBoardDTO.getPublisher());
        originalDTO.setPrice(salesBoardDTO.getPrice());
        originalDTO.setContent(salesBoardDTO.getContent());
        originalDTO.setUpdateDate(salesBoardDTO.getCreateDate());

        
        //  이미지 파일 변경되었을때만 업데이트 되도록
        if (salesBoardDTO.getOimage() != null && !salesBoardDTO.getOimage().isEmpty()) {
            originalDTO.setOimage(salesBoardDTO.getOimage());
        }

        if (salesBoardDTO.getSimage() != null && !salesBoardDTO.getSimage().isEmpty()) {
            originalDTO.setSimage(salesBoardDTO.getSimage());
        }

        salesBoardRepository.save(salesBoardMapper.toEntity(originalDTO));
    }

    // 거래게시판 조회수
    public void updateViewCount(Long sidx, HttpServletRequest req)
    {
        String userId = req.getRemoteAddr(); // IP 주소 가져오기
        String key;

        // 사용자의 로그인 상태 확인
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (sId.equals("anonymousUser")) {
            // 비로그인 사용자일 경우 IP로 키 생성
            key = "viewCount::" + sidx + "::" + userId;
        } else {
            // 로그인 사용자일 경우 ID로 키 생성
            key = "viewCount::" + sidx + "::" + sId;
        }

        // Redis에 데이터가 없을 경우만 조회 수 증가
        if (redisUtil.getData(key) == null) {
            salesBoardRepository.viewCount(sidx); // 조회 수 증가

            redisUtil.setDataExpire(key, "viewed", 1200); // 유효시간 20분
        }
    }

    // 게시글 좋아요
    public void updateLikeCount(Long sidx, HttpServletRequest req)
    {
        String userId = req.getRemoteAddr(); // IP 주소 가져오기
        String key;

        // 사용자의 로그인 상태 확인
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (sId.equals("anonymousUser")) {
            // 비로그인 사용자일 경우 처리하지 않음 (여기서는 호출되지 않음)
            return;
        } else {
            // 로그인 사용자일 경우 ID로 키 생성
            key = "likeCount::" + sidx + "::" + sId;
        }

        // Redis에 데이터가 있는지 확인하여 좋아요 추가 또는 취소
        if (redisUtil.getData(key) == null) {
            // 좋아요 추가
            salesBoardRepository.likeCount(sidx); // 좋아요 수 증가
            redisUtil.setDataExpire(key, "liked", 1200); // 유효시간 20분
        } else {
            // 좋아요 취소
            salesBoardRepository.unlikeCount(sidx); // 좋아요 수 감소
            redisUtil.removeData(key); // Redis에서 제거
        }
    }

    // 게시글 좋아요 취소 
    public void updateDownCount(Long sidx)
    {
        salesBoardRepository.downCount(sidx);
    }

}