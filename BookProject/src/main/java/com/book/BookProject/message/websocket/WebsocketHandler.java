package com.book.BookProject.message.websocket;

import com.book.BookProject.message.MessageDTO;
import com.book.BookProject.message.MessageRepository;
import com.book.BookProject.message.MessageService;
import com.book.BookProject.salesboard.MemberService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebsocketHandler extends TextWebSocketHandler {

    @Autowired
    MessageRepository messageRepository;
    @Autowired
    MessageService messageService;
    @Autowired
    MessageMapper messageMapper;
    @Autowired
    MemberService memberService;


    // 쪽지 접속자 정보를 저장하기 위한 Map
    public static final ConcurrentHashMap<String, WebSocketSession> CLIENTS
            = new ConcurrentHashMap<String, WebSocketSession>();

    // 클라이 언트가 접속했을때
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        CLIENTS.put(userId, session);

        super.afterConnectionEstablished(session);
    }

    // 메세지가 도착했을때 해야할 일들을 정의하는 메소드
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();

        JSONObject jsonMessage = new JSONObject(payload);
        String action = jsonMessage.getString("action"); // 액션 가져오기

        // action이 read일때
        if("read".equals(action)){
            System.out.println("메세지 읽음!!!");

            Long msIdx = jsonMessage.getLong("msIdx");
            String receiverNick = jsonMessage.getString("receiverNick"); // 받는 사람

            // 읽음 상태 업데이트
            markMessagesAsRead(msIdx, receiverNick);
            // 읽음 상태 업데이트 후 JSON 형식으로 메시지 전송
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message", "메시지가 읽혔습니다: " + msIdx);
            session.sendMessage(new TextMessage(jsonObject.toString()));

            // 읽지 않은 쪽지 개수 조회
            long unreadCount = countReadStatus(receiverNick);
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("unreadCount", unreadCount);

            // 수신자에게 읽지 않은 쪽지 개수 업데이트 전송
            WebSocketSession receiverSession = CLIENTS.get(receiverNick);

            if (receiverSession != null && receiverSession.isOpen()) {
                receiverSession.sendMessage(new TextMessage(jsonResponse.toString()));
            }

        } else if("send".equals(action)){

//            System.out.println("메세지 보냄!!!!");

            String senderNick = (String) session.getAttributes().get("userId"); // ID 보낸 사람 현재 세션의 사용자
            String receiverNick = jsonMessage.getString("receiverNick"); // 받는 사람
            String title = jsonMessage.getString("title"); // 받는 사람
            String content = jsonMessage.getString("content"); // 받는 사람

            // DTO에 데이터를 설정
            MessageDTO messageDTO = new MessageDTO();
            messageDTO.setSender(senderNick);
            messageDTO.setReceiver(receiverNick);
            messageDTO.setTitle(title);
            messageDTO.setContent(content);

            // DB에 저장
            messageRepository.save(messageMapper.toEntity(messageDTO));

            TextMessage responseMessage = new TextMessage("From " + senderNick + ": " + payload);

            // 수신자의 세션을 찾고 메시지 전송
            WebSocketSession receiverSession = CLIENTS.get(receiverNick);

            if (receiverSession != null && receiverSession.isOpen()) {
                // 읽지 않은 쪽지 개수 조회
                long unreadCount = countReadStatus(receiverNick);
                // JSON 형식으로 메시지 생성
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("unreadCount", unreadCount);

                // 클라이언트에게 알림 메시지 전송
                receiverSession.sendMessage(new TextMessage(jsonResponse.toString()));

//                System.out.println("세션 목록: " + CLIENTS.keySet()); // 현재 저장된 세션 ID 목록

            } else {
//                System.out.println("전송 실패: " + receiverNick);
            }
        } else {

            String loginId = (String) session.getAttributes().get("userId"); // 현재 로그인 한 아이디
            String loginNick = memberService.findNickById(loginId);

            // 로그인된 아이디를 통해 세션을 찾기
            WebSocketSession messageCountSession = CLIENTS.get(loginId);

            if (messageCountSession != null && messageCountSession.isOpen()) {
                // 읽지 않은 쪽지 개수 조회
                long unreadCount = countReadStatus(loginNick);
                // JSON 형식으로 메시지 생성
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("unreadCount", unreadCount);

                // 클라이언트에게 알림 메시지 전송
                messageCountSession.sendMessage(new TextMessage(jsonResponse.toString()));
            }
        }
    }

    // 안읽은 메시지 갯수 
    public Long countReadStatus(String receiver) {
        return messageRepository.countByReceiverAndReadstatusAndViewstatus(receiver, 0,1);
    }

    // 메세지 읽음 처리
    public void markMessagesAsRead(Long msidx, String receiver) throws Exception {
        messageService.updateReadStatus(msidx);

        Long unreadCount = countReadStatus(receiver);
        WebSocketSession receiverSession = CLIENTS.get(receiver);
        if (receiverSession != null && receiverSession.isOpen()) {
            JSONObject jsonResponse = new JSONObject();
            jsonResponse.put("unreadCount", unreadCount);
            receiverSession.sendMessage(new TextMessage(jsonResponse.toString()));
        }
    }

    // 연결이 끊어지면 실행되는 메소드 !!!!!!!!!!!!!!
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String userId = (String) session.getAttributes().get("userId");
        CLIENTS.remove(userId); // userId를 기준으로 세션 제거
        super.afterConnectionClosed(session, status);
    }
}
