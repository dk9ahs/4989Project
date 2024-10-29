package com.book.BookProject.inquiryboard;

import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface InquiryBoardMapper {

    InquiryBoardDTO toDto(InquiryBoard inquiryBoard);

    InquiryBoard toEntity(InquiryBoardDTO dto);

    List<InquiryBoardDTO> toDtoList(List<InquiryBoard> inquiryBoards);

    List<InquiryBoard> toEntityList(List<InquiryBoardDTO> inquiryBoardDTOS);
}
