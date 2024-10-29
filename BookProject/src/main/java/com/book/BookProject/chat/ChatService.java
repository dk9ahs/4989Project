package com.book.BookProject.chat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Service
public class ChatService {
	
	@Autowired
	private final DatabaseReference databaseReference;
	
	@Autowired 
    public ChatService(DatabaseReference databaseReference) {
        this.databaseReference = databaseReference;
    }

    // 채팅방 목록 가져오기
    public CompletableFuture<List<String>> getChatRoomList() {
        CompletableFuture<List<String>> futureChatRooms = new CompletableFuture<>();
        List<String> chatRooms = new ArrayList<>();
        DatabaseReference chatRoomsRef = databaseReference.child("chatRooms");

        chatRoomsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<CompletableFuture<Void>> futures = new ArrayList<>();

                for (DataSnapshot chatRoomSnapshot : dataSnapshot.getChildren()) {
                    String chatRoomId = chatRoomSnapshot.getKey();

                    CompletableFuture<Void> checkChatRoomFuture = new CompletableFuture<>();
                    futures.add(checkChatRoomFuture);

                    // 각 채팅방의 메시지 조회 (messages 노드 없이 바로 메시지를 탐색)
                    boolean hasViewStatus1 = false;

                    // 메시지 중 viewStatus가 1인 메시지가 있는지 확인
                    for (DataSnapshot messageSnapshot : chatRoomSnapshot.getChildren()) {
                        Integer viewStatus = messageSnapshot.child("viewStatus").getValue(Integer.class);

                        if (viewStatus != null && viewStatus == 1) {
                            hasViewStatus1 = true;
                            break;
                        }
                    }

                    // viewStatus가 1인 메시지가 있는 채팅방만 리스트에 추가
                    if (hasViewStatus1) {
                        chatRooms.add(chatRoomId);
                    }
                    checkChatRoomFuture.complete(null);  // 현재 채팅방 체크 완료
                }

                // 모든 채팅방의 비동기 작업이 완료되면 결과 반환
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).thenRun(() -> {
                    futureChatRooms.complete(chatRooms);  // 모든 채팅방 체크 완료 시 반환
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Firebase error: " + databaseError.getMessage());
                futureChatRooms.completeExceptionally(new RuntimeException("채팅방 리스트를 읽어오던 중 오류 발생"));
            }
        });

        return futureChatRooms;
    }

    // 채팅 보내기
    public void sendMessage(ChatDTO chatDTO) {
        String chatRoomId = chatDTO.getChatRoomId();
        DatabaseReference chatRoomRef = databaseReference.child("chatRooms").child(chatRoomId); // Firebase 경로 설정
        
        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("sender", chatDTO.getSender());
        messageMap.put("message", chatDTO.getMessage());
        messageMap.put("timestamp", chatDTO.getTimestamp());
        messageMap.put("chatRoomId", chatDTO.getChatRoomId());
        messageMap.put("viewStatus", 1);

        chatRoomRef.push().setValueAsync(messageMap);
    }

    // 채팅방 들어가기
    public CompletableFuture<List<ChatDTO>> getMessagesByChatRoomId(String chatRoomId) {
        CompletableFuture<List<ChatDTO>> futureMessages = new CompletableFuture<>();
        List<ChatDTO> messages = new ArrayList<>();
        DatabaseReference chatRoomRef = databaseReference.child("chatRooms").child(chatRoomId);

        chatRoomRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    ChatDTO message = snapshot.getValue(ChatDTO.class);
                    messages.add(message);
                }
                futureMessages.complete(messages);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("채팅방 메세지를 읽어오던 중 오류 발생 : " + databaseError.getMessage());
                futureMessages.completeExceptionally(new RuntimeException("채팅방 메세지를 읽어오던 중 오류 발생"));
            }
        });

        return futureMessages;
    }

    // 삭제시 채팅방 viewStatus를 0으로 업데이트 ( 리스트에서 안보이게)
    public void updateViewStatus(String chatRoomId) {
        // chatRoomId에 해당하는 메시지의 경로를 찾기 위해 chatRooms의 messages 경로를 사용
        DatabaseReference messagesRef = databaseReference.child("chatRooms").child(chatRoomId);

        // 특정 채팅방의 모든 메시지에 대해 viewStatus를 0으로 업데이트
        messagesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot messageSnapshot : dataSnapshot.getChildren()) {
                    // 각 메시지의 viewStatus를 0으로 설정
                    String messageId = messageSnapshot.getKey();
                    messagesRef.child(messageId).child("viewStatus").setValueAsync(0).addListener(() -> {
//                        System.out.println("Message " + messageId + " updated to viewStatus 0.");
                    }, Runnable::run);
                }
//                System.out.println("All messages updated to viewStatus 0.");
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.err.println("Failed to read messages: " + databaseError.getMessage());
            }
        });
    }
}
