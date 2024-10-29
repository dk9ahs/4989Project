package com.book.BookProject.salesboard;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SalesBoardMapper {

    SalesBoardDTO toDto(SalesBoard salesBoard);

    SalesBoard toEntity(SalesBoardDTO dto);

    List<SalesBoardDTO> toDtoList(List<SalesBoard> salesBoards);

    List<SalesBoard> toEntityList(List<SalesBoardDTO> salesBoardDTOS);
}
