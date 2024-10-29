package com.book.BookProject.message.websocket;

import com.book.BookProject.message.Message;
import com.book.BookProject.message.MessageDTO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2024-10-22T09:56:41+0900",
    comments = "version: 1.5.5.Final, compiler: javac, environment: Java 17.0.12 (Homebrew)"
)
@Component
public class MessageMapperImpl implements MessageMapper {

    @Override
    public MessageDTO toDto(Message message) {
        if ( message == null ) {
            return null;
        }

        MessageDTO messageDTO = new MessageDTO();

        messageDTO.setMsidx( message.getMsidx() );
        messageDTO.setSender( message.getSender() );
        messageDTO.setReceiver( message.getReceiver() );
        messageDTO.setTitle( message.getTitle() );
        messageDTO.setContent( message.getContent() );
        messageDTO.setCreateDate( message.getCreateDate() );
        messageDTO.setReadstatus( message.getReadstatus() );
        messageDTO.setViewstatus( message.getViewstatus() );
        messageDTO.setSendviewstatus( message.getSendviewstatus() );

        return messageDTO;
    }

    @Override
    public Message toEntity(MessageDTO dto) {
        if ( dto == null ) {
            return null;
        }

        Message.MessageBuilder message = Message.builder();

        message.msidx( dto.getMsidx() );
        message.sender( dto.getSender() );
        message.receiver( dto.getReceiver() );
        message.title( dto.getTitle() );
        message.content( dto.getContent() );
        message.createDate( dto.getCreateDate() );
        message.readstatus( dto.getReadstatus() );
        message.viewstatus( dto.getViewstatus() );
        message.sendviewstatus( dto.getSendviewstatus() );

        return message.build();
    }

    @Override
    public List<MessageDTO> toDtoList(List<Message> messages) {
        if ( messages == null ) {
            return null;
        }

        List<MessageDTO> list = new ArrayList<MessageDTO>( messages.size() );
        for ( Message message : messages ) {
            list.add( toDto( message ) );
        }

        return list;
    }

    @Override
    public List<Message> toEntityList(List<MessageDTO> messageDTOS) {
        if ( messageDTOS == null ) {
            return null;
        }

        List<Message> list = new ArrayList<Message>( messageDTOS.size() );
        for ( MessageDTO messageDTO : messageDTOS ) {
            list.add( toEntity( messageDTO ) );
        }

        return list;
    }
}
