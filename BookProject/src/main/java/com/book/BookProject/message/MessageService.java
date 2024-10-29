package com.book.BookProject.message;

import com.book.BookProject.message.websocket.MessageMapper;
import com.book.BookProject.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@Transactional
public class MessageService{

    @Autowired
    MessageRepository messageRepository;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    UserRepository userRepository;


    // 보낸 쪽지 리스트
    public Page<MessageDTO> messageList(String receiver, int page){
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "msidx"));
//        Page<Message> messagePage = messageRepository.findByReceiverContaining(receiver, pageable);
        Page<Message> messagePage = messageRepository.findByReceiverContainingAndViewstatus(receiver, 1, pageable);


        List<MessageDTO> dtomessage = messageMapper.toDtoList(messagePage.getContent());

        return new PageImpl<>(dtomessage, messagePage.getPageable(), messagePage.getTotalElements());
    }

    // 받은 쪽지 리스트
    public Page<MessageDTO> messagesentList(String sender, int page){
        Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "msidx"));
        Page<Message> messagePage = messageRepository.findBySenderContainingAndSendviewstatus(sender, 1, pageable);

        List<MessageDTO> dtomessage = messageMapper.toDtoList(messagePage.getContent());

        return new PageImpl<>(dtomessage, messagePage.getPageable(), messagePage.getTotalElements());
    }

    // 쪽지 상세 보기
    public MessageDTO getMessageByIdx(Long msidx){
        Message message = messageRepository.findById(msidx).get();

        return messageMapper.toDto(message);
    }

    // 안읽은 쪽지 카운트
    public Long countReadStatus(String receiver){
        return messageRepository.countByReceiverAndReadstatusAndViewstatus(receiver, 1, 0);
    }

    // 읽음 쪽지 표시
    public void updateReadStatus(Long msidx){
        messageRepository.updateReadstatus(msidx);
    }

    // 받은 메세지 리스트에서 숨기기
    public void hideMessages(List<Long> msidxList) {
        for (Long msidx : msidxList) {
            messageRepository.updateViewstatus(msidx);
        }
    }

    // 보낸 메세지 리스트에서 숨기기
    public void sendHideMessages(List<Long> msidxList) {
        for (Long msidx : msidxList) {
            messageRepository.updateSendViewstatus(msidx);
        }
    }

    // 존재하는 회원 유무
    public boolean isNickExist(String nick) {
        return userRepository.existsByNick(nick);
    }


}
