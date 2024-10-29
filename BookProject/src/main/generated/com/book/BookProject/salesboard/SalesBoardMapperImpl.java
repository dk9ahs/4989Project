package com.book.BookProject.salesboard;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-09T19:37:24+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.11 (Oracle Corporation)"
)
@Component
public class SalesBoardMapperImpl implements SalesBoardMapper {

    @Override
    public SalesBoardDTO toDto(SalesBoard salesBoard) {
        if ( salesBoard == null ) {
            return null;
        }

        SalesBoardDTO salesBoardDTO = new SalesBoardDTO();

        salesBoardDTO.setSidx( salesBoard.getSidx() );
        salesBoardDTO.setNick( salesBoard.getNick() );
        salesBoardDTO.setTitle( salesBoard.getTitle() );
        salesBoardDTO.setBooktitle( salesBoard.getBooktitle() );
        salesBoardDTO.setAuthor( salesBoard.getAuthor() );
        salesBoardDTO.setPublisher( salesBoard.getPublisher() );
        salesBoardDTO.setContent( salesBoard.getContent() );
        salesBoardDTO.setClassification( salesBoard.getClassification() );
        salesBoardDTO.setRegion( salesBoard.getRegion() );
        salesBoardDTO.setPrice( salesBoard.getPrice() );
        salesBoardDTO.setCreateDate( salesBoard.getCreateDate() );
        salesBoardDTO.setUpdateDate( salesBoard.getUpdateDate() );
        salesBoardDTO.setViewCount( salesBoard.getViewCount() );
        salesBoardDTO.setLikeCount( salesBoard.getLikeCount() );
        salesBoardDTO.setOimage( salesBoard.getOimage() );
        salesBoardDTO.setSimage( salesBoard.getSimage() );
        salesBoardDTO.setDownCount( salesBoard.getDownCount() );

        return salesBoardDTO;
    }

    @Override
    public SalesBoard toEntity(SalesBoardDTO dto) {
        if ( dto == null ) {
            return null;
        }

        SalesBoard.SalesBoardBuilder salesBoard = SalesBoard.builder();

        salesBoard.sidx( dto.getSidx() );
        salesBoard.nick( dto.getNick() );
        salesBoard.title( dto.getTitle() );
        salesBoard.booktitle( dto.getBooktitle() );
        salesBoard.author( dto.getAuthor() );
        salesBoard.publisher( dto.getPublisher() );
        salesBoard.content( dto.getContent() );
        salesBoard.classification( dto.getClassification() );
        salesBoard.region( dto.getRegion() );
        salesBoard.price( dto.getPrice() );
        salesBoard.createDate( dto.getCreateDate() );
        salesBoard.updateDate( dto.getUpdateDate() );
        salesBoard.viewCount( dto.getViewCount() );
        salesBoard.likeCount( dto.getLikeCount() );
        salesBoard.oimage( dto.getOimage() );
        salesBoard.simage( dto.getSimage() );
        salesBoard.downCount( dto.getDownCount() );

        return salesBoard.build();
    }

    @Override
    public List<SalesBoardDTO> toDtoList(List<SalesBoard> salesBoards) {
        if ( salesBoards == null ) {
            return null;
        }

        List<SalesBoardDTO> list = new ArrayList<SalesBoardDTO>( salesBoards.size() );
        for ( SalesBoard salesBoard : salesBoards ) {
            list.add( toDto( salesBoard ) );
        }

        return list;
    }

    @Override
    public List<SalesBoard> toEntityList(List<SalesBoardDTO> salesBoardDTOS) {
        if ( salesBoardDTOS == null ) {
            return null;
        }

        List<SalesBoard> list = new ArrayList<SalesBoard>( salesBoardDTOS.size() );
        for ( SalesBoardDTO salesBoardDTO : salesBoardDTOS ) {
            list.add( toEntity( salesBoardDTO ) );
        }

        return list;
    }
}
