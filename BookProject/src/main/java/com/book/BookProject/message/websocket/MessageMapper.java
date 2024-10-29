package com.book.BookProject.message.websocket;

import com.book.BookProject.message.Message;
import com.book.BookProject.message.MessageDTO;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MessageMapper {

    MessageDTO toDto(Message message);

    Message toEntity(MessageDTO dto);

    List<MessageDTO> toDtoList(List<Message> messages);

    List<Message> toEntityList(List<MessageDTO> messageDTOS);
}
