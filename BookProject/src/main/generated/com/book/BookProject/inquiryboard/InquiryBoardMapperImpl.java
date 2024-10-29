package com.book.BookProject.inquiryboard;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-22T14:23:59+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.11 (Oracle Corporation)"
)
@Component
public class InquiryBoardMapperImpl implements InquiryBoardMapper {

    @Override
    public InquiryBoardDTO toDto(InquiryBoard inquiryBoard) {
        if ( inquiryBoard == null ) {
            return null;
        }

        InquiryBoardDTO inquiryBoardDTO = new InquiryBoardDTO();

        inquiryBoardDTO.setQidx( inquiryBoard.getQidx() );
        inquiryBoardDTO.setParentIdx( inquiryBoard.getParentIdx() );
        inquiryBoardDTO.setParentId( inquiryBoard.getParentId() );
        inquiryBoardDTO.setNick( inquiryBoard.getNick() );
        inquiryBoardDTO.setResponses( inquiryBoard.getResponses() );
        inquiryBoardDTO.setTitle( inquiryBoard.getTitle() );
        inquiryBoardDTO.setContent( inquiryBoard.getContent() );
        inquiryBoardDTO.setPass( inquiryBoard.getPass() );
        inquiryBoardDTO.setCreateDate( inquiryBoard.getCreateDate() );
        inquiryBoardDTO.setUpdateDate( inquiryBoard.getUpdateDate() );
        inquiryBoardDTO.setViewCount( inquiryBoard.getViewCount() );
        inquiryBoardDTO.setLikeCount( inquiryBoard.getLikeCount() );
        inquiryBoardDTO.setOFile( inquiryBoard.getOFile() );
        inquiryBoardDTO.setSFile( inquiryBoard.getSFile() );
        inquiryBoardDTO.setDownCount( inquiryBoard.getDownCount() );

        return inquiryBoardDTO;
    }

    @Override
    public InquiryBoard toEntity(InquiryBoardDTO dto) {
        if ( dto == null ) {
            return null;
        }

        InquiryBoard.InquiryBoardBuilder inquiryBoard = InquiryBoard.builder();

        inquiryBoard.qidx( dto.getQidx() );
        inquiryBoard.origin( dto.getOrigin() );
        inquiryBoard.group( dto.getGroup() );
        inquiryBoard.layer( dto.getLayer() );
        inquiryBoard.responses( dto.getResponses() );
        inquiryBoard.nick( dto.getNick() );
        inquiryBoard.title( dto.getTitle() );
        inquiryBoard.content( dto.getContent() );
        inquiryBoard.pass( dto.getPass() );
        inquiryBoard.createDate( dto.getCreateDate() );
        inquiryBoard.updateDate( dto.getUpdateDate() );
        inquiryBoard.viewCount( dto.getViewCount() );
        inquiryBoard.ofile( dto.getOfile() );
        inquiryBoard.sfile( dto.getSfile() );

        return inquiryBoard.build();
    }

    @Override
    public List<InquiryBoardDTO> toDtoList(List<InquiryBoard> inquiryBoards) {
        if ( inquiryBoards == null ) {
            return null;
        }

        List<InquiryBoardDTO> list = new ArrayList<InquiryBoardDTO>( inquiryBoards.size() );
        for ( InquiryBoard inquiryBoard : inquiryBoards ) {
            list.add( toDto( inquiryBoard ) );
        }

        return list;
    }

    @Override
    public List<InquiryBoard> toEntityList(List<InquiryBoardDTO> inquiryBoardDTOS) {
        if ( inquiryBoardDTOS == null ) {
            return null;
        }

        List<InquiryBoard> list = new ArrayList<InquiryBoard>( inquiryBoardDTOS.size() );
        for ( InquiryBoardDTO inquiryBoardDTO : inquiryBoardDTOS ) {
            list.add( toEntity( inquiryBoardDTO ) );
        }

        return list;
    }
}
