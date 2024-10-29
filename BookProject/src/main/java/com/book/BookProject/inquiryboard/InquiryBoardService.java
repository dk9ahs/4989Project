package com.book.BookProject.inquiryboard;

import com.book.BookProject.salesboard.Redis.RedisUtil;
import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class InquiryBoardService {

    @Autowired
    private InquiryBoardRepository inquiryBoardRepository;
    @Autowired
    private InquiryBoardMapper inquiryBoardMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    RedisUtil redisUtil;

    // 문의게시판 리스트
    public Page<InquiryBoardDTO> inquiryBoardList(int page, String searchField, String searchWord)
    {
        Pageable pageable = PageRequest.of(page, 10);
        Page<InquiryBoard> inquiryBoardPage;

        if(searchField != null && searchWord != null && !searchWord.isEmpty())
        {
            switch (searchField)
            {
                case "title":
                    inquiryBoardPage = inquiryBoardRepository.findByTitleContaining(searchWord, pageable);
                    break;
                case "content":
                    inquiryBoardPage = inquiryBoardRepository.findByContentContaining(searchWord, pageable);
                    break;
                case "nick":
                    inquiryBoardPage = inquiryBoardRepository.findByNickContaining(searchWord, pageable);
                    break;
                default:
                    inquiryBoardPage = inquiryBoardRepository.findAllOrdered(pageable);
            }
        }else{
            inquiryBoardPage = inquiryBoardRepository.findAllOrdered(pageable);
        }

        // 엔티티를 dto로 변환
        List<InquiryBoardDTO> dtoList = inquiryBoardMapper.toDtoList(inquiryBoardPage.getContent());

        return new PageImpl<>(dtoList, inquiryBoardPage.getPageable(), inquiryBoardPage.getTotalElements());
    }

    // 문의게시판 상세보기
    @Transactional(readOnly = true)
    public InquiryBoardDTO inquiryBoardView(Long qidx)
    {
        InquiryBoard inquiryBoard = inquiryBoardRepository.findById(qidx).get();

        return inquiryBoardMapper.toDto(inquiryBoard);
    }

    //문의게시판 글 작성
    public void inquiryBoardWrite(InquiryBoardDTO inquiryBoardDTO)
    {
        if(inquiryBoardDTO.getPass().equals(""))
        {
            inquiryBoardDTO.setPass(null);
        }

        // dto에서 엔티티로 저장
        InquiryBoard inquiryBoard = inquiryBoardMapper.toEntity(inquiryBoardDTO);
        InquiryBoard saveBoard = inquiryBoardRepository.save(inquiryBoard); // 1차 저장

        // dto로 변환 후 Origin 설정 인덱스를 origin에 넣기 위해서
        InquiryBoardDTO saveDTO = inquiryBoardMapper.toDto(saveBoard);
        if (saveDTO.getOrigin() == null)
        {

            saveDTO.setOrigin(saveDTO.getQidx());
            saveDTO.setUpdateDate(saveDTO.getCreateDate()); // 작성일과 수정일이 다르게 들어 갈 때 오류 방지로 한번 더 넣어준다.

            // dto -> 엔티티로 변환 후 저장
            InquiryBoard updateBoard = inquiryBoardMapper.toEntity(saveDTO);
            inquiryBoardRepository.save(updateBoard); // 2차 저장
        }

    }

    //문의게시판 글 수정
    public void inquiryBoardEditor(Long qidx, InquiryBoardDTO inquiryBoardDTO)
    {
        InquiryBoard inquiryBoard = inquiryBoardRepository.findById(qidx).get();
        InquiryBoardDTO originalDTO = inquiryBoardMapper.toDto(inquiryBoard);

        originalDTO.setQidx(qidx);
        originalDTO.setNick(inquiryBoardDTO.getNick());

        if(inquiryBoardDTO.getPass().equals(""))
        {
            originalDTO.setPass(null);
        } else {
            originalDTO.setPass(inquiryBoardDTO.getPass());
        }
        originalDTO.setTitle(inquiryBoardDTO.getTitle());
        originalDTO.setContent(inquiryBoardDTO.getContent());
        originalDTO.setUpdateDate(inquiryBoardDTO.getCreateDate());

        // 이미지 파일 변경 시에만 업데이트
        if(inquiryBoardDTO.getOfile() != null && !inquiryBoardDTO.getOfile().isEmpty())
        {
            originalDTO.setOfile(inquiryBoardDTO.getOfile());
        }

        if(inquiryBoardDTO.getSfile() != null && !inquiryBoardDTO.getSfile().isEmpty())
        {
            originalDTO.setSfile(inquiryBoardDTO.getSfile());
        }

        inquiryBoardRepository.save(inquiryBoardMapper.toEntity(originalDTO));
    }

    // 문의게시판 글 삭제
    public void inquiryBoardDelete(Long qidx)
    {
        inquiryBoardRepository.deleteById(qidx);
    }

    // 문의게시판 조회수
    @Transactional
    public void inquiryBoardViewCount(Long qidx)
    {
        // 사용자의 로그인 상태 확인
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        String key = "viewCount::" + qidx + "::" + sId;

        // Redis에 데이터가 없을 경우만 조회 수 증가
        if (redisUtil.getData(key) == null)
        {
            inquiryBoardRepository.viewCount(qidx); // 조회 수 증가

            redisUtil.setDataExpire(key, "viewed", 1200); // 유효시간 20분
        }
    }

    // 비밀 글 일 때 로그인 된 닉네임과 비교 하기 위해
    public String findNickById(String id)
    {
        UserEntity userEntity = userRepository.findById(id).orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        return userEntity.getNick();
    }

    // 답변 상태 업데이트
    @Transactional
    public void updateResponseStatus(Long originNo)
    {
        inquiryBoardRepository.updateResponse(originNo);
    }
}