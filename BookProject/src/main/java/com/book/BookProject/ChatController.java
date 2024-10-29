package com.book.BookProject;

import com.book.BookProject.chat.ChatDTO;
import com.book.BookProject.chat.ChatService;
import com.book.BookProject.salesboard.MemberService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@Controller
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    @Autowired
    private ChatService chatService;

    @Autowired
    MemberService memberService;

    // 채팅방 전 확인 팝업
    @RequestMapping("/chatPopup")
    public String chatPopup(Model model) {
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        String nick = memberService.findNickById(sId);

        model.addAttribute("loginNick", nick);

        return "member/chatPopup";
    }

    @RequestMapping("/chatRoom")
    public CompletableFuture<Object> chatRoom(HttpServletRequest request, Model model)
    {
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        String loginNick = memberService.findNickById(sId);
        String chatRoomId = request.getParameter("chatRoomId");

        model.addAttribute("loginNick", loginNick);
        model.addAttribute("chatRoomId", chatRoomId);

        return chatService.getMessagesByChatRoomId(chatRoomId)
                .thenApply(messages -> {
                    model.addAttribute("messages", messages);
                    return "member/chatRoom";
                });
    }

    // 메세지 보내는 기능
    @PostMapping("/send")
    public ResponseEntity<ChatDTO> sendMessage(@RequestBody ChatDTO chatDTO) {
        chatService.sendMessage(chatDTO);
        return ResponseEntity.ok(chatDTO);
    }

    @RequestMapping("/chatRoomList")
    public CompletableFuture<String> chatRoomList(Model model) {
        return chatService.getChatRoomList()
                .thenApply(chatRooms -> {
                    model.addAttribute("chatRooms", chatRooms);
                    return "admin/chatRoomList";
                });
    }

    @DeleteMapping("/deleteChatRoom")
    public ResponseEntity<String> deleteChatRoom(HttpServletRequest request, @RequestParam String chatRoomId) {
        try {
            System.out.println("채팅방삭제");
            chatService.updateViewStatus(chatRoomId);

            return ResponseEntity.ok("채팅방이 성공적으로 삭제되었습니다.");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("채팅방 삭제에 실패했습니다: " + e.getMessage());
        }
    }
}
