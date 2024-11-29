# SpringBoot-Project-4989 
스프링 부트 + Thymeleaf 중고도서 거래, 나눔, 교환을 위한 웹사이트 개발

## 프로젝트 소개
절판된 도서나 희귀 도서들을 발견하고 위치나 시간의 제약없이 거래할 수 있는 사이트입니다.

## 개발 기간 
2024.9.23 - 2024.10.24

### 개발 환경
- `Java 17`
- `JDK 17.0.11`
- **IDE** : IntelliJ Community
- **Framework** : Springboot(3.3.2)
- **Database** : MySQL(8.0.37)
- **ORM** : JPA

## 담당 기능
- **WebSocketd을 이용한 쪽지 알람과 쪽지 기능**
    - **handleTextMessage**을 사용해 현재 로그인한 사용자의 읽지 않은 메시지 개수를 조회하여 **JSON** 형식으로 클라이언트에 전송했습니다.
    - 메세지 **전송** 시  메시지를 데이터베이스에 저장한 후, 수신자의 **WebSocket 세션**을 찾아 해당 메시지를 전송하였고 읽지 않은 메세지 개수도 업데이트 하였습니다.
    - 메세지 **읽음** 시 발신자와 수신자 정보, 제목 및 내용을 받아 DTO로 변환 후 **데이터 베이스**에 저장하였습니다. 읽음 상태 업데이트 후, 수신자에게 읽지 않은 메시지 개수를 전송하였습니다.

     ### code
    <details>
    <summary>WebsocketConfig.java</summary>

    @Configuration
    @EnableWebSocket
    public class WebsocketConfig implements WebSocketConfigurer {
    
        // 웹소켓 메세지를 처리하는 핸들러 선언, 의존성 주입
        private final WebsocketHandler websocketHandler;
    
        // 생성자를 통해 WebSocketMessageHandler 인스턴스를 주입받음
        public WebsocketConfig(WebsocketHandler websocketHandler) {
            this.websocketHandler = websocketHandler;
        }
    
        @Override
        public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
            registry.addHandler(websocketHandler, "/test")
                    .addInterceptors(new WebsocketHandshakeInterceptor()) // HttpSession 기반 인증 정보 전달
                    .setAllowedOrigins("http://localhost:8083");
        }
    }
    </details>

    <details>
    <summary>WebsocketHandler.java</summary>

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
    </details>

    <details>
    <summary>WebsocketHandshakeInterceptor.java</summary>

        public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {
            @Override
            public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {
    
                // 사용자 정보 가져오기
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
                if (authentication != null && !(authentication instanceof AnonymousAuthenticationToken) && authentication.isAuthenticated()) {
                    // 인증된 사용자면 userId 속성에 추가
                    String userId = authentication.getName();
                    attributes.put("userId", userId);
                    return true; // WebSocket 연결 허용
                } else {
                    // 인증되지 않은 사용자면 WebSocket 연결 차단
                    return false; // WebSocket 연결 차단
                }
            }
        }
    </details>
    
    <details>
    <summary>MessageController.java</summary>

        @Controller
        @RequestMapping("/messages")
        @RequiredArgsConstructor
        public class MessageController {
            @Autowired
            private MessageService messageService;
            @Autowired
            MemberService memberService;
        
            // 쪽지 보낼 폼
            @GetMapping("/form")
            public String form(Model model, String receiver) {
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                String nick = memberService.findNickById(sId);
                model.addAttribute("loginNick", nick);
        
                model.addAttribute("receiver", receiver);
        
        
                return "member/MessageForm";
            }
        
            // 쪽지 읽기
            @GetMapping("/view")
            public String view(Model model, Long msidx) {
        
                // 쪽지 상세보기
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                String nick = memberService.findNickById(sId);
                model.addAttribute("loginNick", nick);
                
                model.addAttribute("messages", messageService.getMessageByIdx(msidx));
        
                return "member/MessageView";
            }
        
            // 보낸 쪽지 읽기
            @GetMapping("/sendview")
            public String sendview(Model model, Long msidx) {
        
                // 쪽지 상세보기
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                String nick = memberService.findNickById(sId);
                model.addAttribute("loginNick", nick);
        
                model.addAttribute("messages", messageService.getMessageByIdx(msidx));
        
                return "member/MessageSendView";
            }
        
            // 받은 쪽지함
            @GetMapping("/list")
            public String list(Model model,
                               @RequestParam(defaultValue = "1") int page) {
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                String nick = memberService.findNickById(sId);
        
                if (sId.equals("anonymousUser")) {
                    model.addAttribute("loginNick", "Guest");
                } else {
                    model.addAttribute("loginNick", nick);
                }
        
                Page<MessageDTO> listPage = messageService.messageList(nick, page-1);
                model.addAttribute("messageList", listPage.getContent());
        
                long totalCount = listPage.getTotalElements();
                int totalPage = listPage.getTotalPages();
                int currentGroup = (page - 1) / 5; // 현재 그룹 (0부터 시작)
                int pageSize = listPage.getSize();
        
                model.addAttribute("salesBoards", listPage.getContent());
                model.addAttribute("totalPage", totalPage); // 총 페이지
                model.addAttribute("currentPage", page); // 현재 페이지 추가
                model.addAttribute("currentGroup", currentGroup);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("pageSize", pageSize);
        
                Long count = messageService.countReadStatus(nick);
                model.addAttribute("count", count);
                System.out.println(count);
        
                return "member/MessageList";
            }
            // 보낸 쪽지함
            @GetMapping("/sentlist")
            public String sentlist(Model model,
                               @RequestParam(defaultValue = "1") int page) {
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                String nick = memberService.findNickById(sId);
                model.addAttribute("loginNick", nick);
        
                Page<MessageDTO> listPage = messageService.messagesentList(nick, page-1);
                model.addAttribute("messageList", listPage.getContent());
        
                long totalCount = listPage.getTotalElements();
                int totalPage = listPage.getTotalPages();
                int currentGroup = (page - 1) / 5; // 현재 그룹 (0부터 시작)
                int pageSize = listPage.getSize();
        
                model.addAttribute("salesBoards", listPage.getContent());
                model.addAttribute("totalPage", totalPage); // 총 페이지
                model.addAttribute("currentPage", page); // 현재 페이지 추가
                model.addAttribute("currentGroup", currentGroup);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("pageSize", pageSize);
        
                Long count = messageService.countReadStatus(nick);
                model.addAttribute("count", count);
                System.out.println(count);
        
                return "member/MessageSentList";
            }
        
            // 쪽지 도착시 리스트 reload
            @GetMapping("/relist")
            public String relist(Model model,
                               @RequestParam(defaultValue = "1") int page) {
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                String nick = memberService.findNickById(sId);
        
                if (sId.equals("anonymousUser")) {
                    model.addAttribute("loginNick", "Guest");
                } else {
                    model.addAttribute("loginNick", nick);
                }
        
                Page<MessageDTO> listPage = messageService.messageList(nick, page-1);
                model.addAttribute("messageList", listPage.getContent());
        
                long totalCount = listPage.getTotalElements();
                int totalPage = listPage.getTotalPages();
                int currentGroup = (page - 1) / 5; // 현재 그룹 (0부터 시작)
                int pageSize = listPage.getSize();
        
                model.addAttribute("salesBoards", listPage.getContent());
                model.addAttribute("totalPage", totalPage); // 총 페이지
                model.addAttribute("currentPage", page); // 현재 페이지 추가
                model.addAttribute("currentGroup", currentGroup);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("pageSize", pageSize);
        
                Long count = messageService.countReadStatus(nick);
                model.addAttribute("count", count);
                System.out.println(count);
        
                return "member/MessageList :: #messageListTable";
            }
        
            // 받은 메세지 list에서 삭제
            @DeleteMapping("/delete")
            public ResponseEntity<Void> deleteMessages(@RequestBody List<Long> msidxList){
                messageService.hideMessages(msidxList);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
            // 보낸 메세지 list에서 삭제
            @DeleteMapping("/senddelete")
            public ResponseEntity<Void> senddeleteMessages(@RequestBody List<Long> msidxList){
                messageService.sendHideMessages(msidxList);
                return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
            }
        
            // 회원 확인
            @GetMapping("/checkNick")
            public boolean checkNickExist(@RequestParam String nick) {
        
                return messageService.isNickExist(nick);
            }
        
        }
    </details>

    <details>
    <summary>MessageRepository.java</summary>

        @Repository
        public interface MessageRepository extends JpaRepository<Message, Long> {
            // 받은 쪽지
            Page<Message> findByReceiverContainingAndViewstatus(String receiver, int viewstatus, Pageable pageable);
        
            // 보낸 쪽지
            Page<Message> findBySenderContainingAndSendviewstatus(String sender, int sendviewstatus, Pageable pageable);
        
            // 내게 쓴 쪽지 리스트
            @Query("SELECT m FROM MESSAGE m WHERE (m.receiver LIKE %:receiver% OR m.sender = m.receiver) AND m.receiver LIKE %:receiver%")
            Page<Message> findByReceiverOrSender(String receiver, Pageable pageable);
            
            // 안읽은 쪽지 카운트
            long countByReceiverAndReadstatusAndViewstatus(String receiver, int readstatus, int viewstatus);
        
            // 쪽지 읽음으로 표시
            @Modifying
            @Query(value = "update MESSAGE set readstatus = 1 where msidx=:msidx", nativeQuery = true)
            int updateReadstatus(@Param("msidx") Long msidx);
        
            // 받은쪽지 list에서 삭제
            @Modifying
            @Query(value = "update MESSAGE set viewstatus = 0 where msidx=:msidx", nativeQuery = true)
            int updateViewstatus(@Param("msidx") Long msidx);
        
            // 보낸 쪽지 list에서 삭제
            @Modifying
            @Query(value = "update MESSAGE set sendviewstatus = 0 where msidx=:msidx", nativeQuery = true)
            int updateSendViewstatus(@Param("msidx") Long msidx);
        
        }
    </details>

    <details>
    <summary>MessageService.java</summary>

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
    </details>
    
    
- **Firebase Realtime을 이용한 실시간 채팅 기능 구현**
    - 프로젝트 스마일 로드에서의 채팅 기능은 유사하지만 문의채팅방은 사용자들이 자주 묻는 내용을 파악하기 위해서 데이터베이스에 채팅 내용을 저장하는 필요성을 느꼈습니다.
    - 프로젝트 사구팔구에서의 채팅 기능은 채팅 종료 시에 채팅방에서의 기록은 삭제되도록 하였지만, 채팅 내용을 firebase에 저장하여 데이터를 보존하였습니다.
 
     ### code
    <details>
    <summary>ChatController.java</summary>

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
    </details>


    <details>
    <summary>ChatService.java</summary>

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
                            }, Runnable::run);
                        }
                    }
        
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        System.err.println("Failed to read messages: " + databaseError.getMessage());
                    }
                });
            }
        }
    </details>

- **MapStruct와 JPA를 이용한 거래 게시판  CRUD 구현**
    - **PageRequest**객체를 사용하여 페이지 번호와 페이지 크기를 설정하여 원하는 범위의 데이터를 불러와 게시판 목록을 구현하였습니다.
    - **Redis**를 활용하여 조회수 중복 방지 기능을 구현하였습니다. 일정 시간 동안 동일 게시글에 대해 중복 조회를 하지 못하도록 하였습니다.
    - **Spring Security**를 이용해 비로그인 사용자는 좋아요를 누를 수 없도록 하였습니다.  로그인 사용자는 **Redis**를 통해 이미 좋아요를 누른 게시글에 대해 좋아요 취소가 가능하게 하였습니다.
    - 파일 업로드 시 **UUID**를 사용해 저장된 파일명을 중복되지 않도록 하였고 지정된 경로에 파일을 저장 하였습니다. 다운로드 기능도 구현하였습니다.

    ### code
    <details>
    <summary>SalesBoardController.java</summary>

        @Controller
        @RequestMapping("/salesboard")
        @RequiredArgsConstructor
        public class SalesBoardController {
        
            @Autowired
            SalesBoardService salesBoardService;
            @Autowired
            MemberService memberService;
            @Autowired
            ServletContext context;
            @Autowired
            RedisUtil redisUtil;
        
            // 게시판 목록
            @GetMapping
            public String list(Model model,
                               HttpServletRequest request,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String searchField,
                               @RequestParam(required = false) String searchWord){
        
                Page<SalesBoardDTO> listPage = salesBoardService.getAllSalesBoards(page -1, searchField, searchWord);
        
                long totalCount = listPage.getTotalElements();
                int totalPage = listPage.getTotalPages();
                int currentGroup = (page - 1) / 5; // 현재 그룹 (0부터 시작)
                int pageSize = listPage.getSize();
        
                model.addAttribute("salesBoards", listPage.getContent());
                model.addAttribute("totalPage", totalPage); // 총 페이지
                model.addAttribute("currentPage", page); // 현재 페이지 추가
                model.addAttribute("currentGroup", currentGroup);
                model.addAttribute("totalCount", totalCount);
                model.addAttribute("pageSize", pageSize);
        
                model.addAttribute("searchField", searchField); // 검색필드
                model.addAttribute("searchWord", searchWord); // 검색어
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                if (sId.equals("anonymousUser")) {
                    model.addAttribute("loginNick", "Guest");
                    model.addAttribute("liked", false);
                } else {
                    String nick = memberService.findNickById(sId);
                    model.addAttribute("loginNick", nick);
        
                    List<SalesBoardDTO> salesBoards = listPage.getContent();
                    List<Boolean> likedStatusList = new ArrayList<>();
        
                    for (SalesBoardDTO post : salesBoards) {
                        String userId = sId; // 로그인한 사용자 ID
                        boolean liked = redisUtil.getData("likeCount::" + post.getSidx() + "::" + userId) != null;
                        likedStatusList.add(liked);
                    }
                    model.addAttribute("likedList", likedStatusList);
                }
                return "/guest/salesboardlist";
            }
        
            // 글쓰기 폼 이동
            @GetMapping("/write")
            public String createForm(Model model) {
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName(); // 로그인한 아이디 찾기
                String nick = memberService.findNickById(sId); // 아이디로 닉네임 찾기
                model.addAttribute("nick", nick);
        
                return "member/salesboardwriteform";
            }
        
            // 글쓰기 처리
            @PostMapping("/write")
            public String create(HttpServletRequest request, SalesBoardDTO  salesBoardDTO,  @RequestParam("file") MultipartFile file) throws FileNotFoundException {
        
                if (file != null && !file.isEmpty()) {
                    String oImageName = file.getOriginalFilename();
                    String uploadDir = new File("src/main/resources/static/images").getAbsolutePath(); // 이미지 저장 경로 지정
                    System.out.println(uploadDir);
        
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
        
                    String sImageName = UUID.randomUUID().toString() + "_" + oImageName;
        
                    File destination = new File(dir, sImageName);
                    try {
                        file.transferTo(destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "redirect:/salesboard/write?status=fail";
                    }
        
                    // 파일이 있을 경우에만 DTO에 이미지 설정
                    salesBoardDTO.setOimage(oImageName);
                    salesBoardDTO.setSimage(sImageName);
                } else {
                    // 파일이 없을 때 처리할 내용 (필요한 경우)
                    System.out.println("No file uploaded.");
                }
        
                // 지역 데이터
                String sido = request.getParameter("sido1");
                String gugun = request.getParameter("gugun1");
                System.out.println(sido);
                System.out.println(gugun);
        
                salesBoardDTO.setRegion(sido + " " + gugun);
                // 가격 데이터 default 값 넣기
                Integer price = (salesBoardDTO.getPrice() == null) ? 0 : salesBoardDTO.getPrice();
                salesBoardDTO.setPrice(price);
        
                salesBoardService.createSalesBoard(salesBoardDTO);
                return "redirect:/salesboard";
            }
            
            // 글 상세보기
            @GetMapping("/detail")
            public String detail(Long sidx, Model model, HttpServletRequest req, HttpServletRequest res) {
                salesBoardService.updateViewCount(sidx, req);
        
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                if (sId.equals("anonymousUser")) {
                    model.addAttribute("loginNick", "Guest");
                    model.addAttribute("liked", false);
                } else {
                    String nick = memberService.findNickById(sId);
                    model.addAttribute("loginNick", nick);
        
                    boolean liked = redisUtil.getData("likeCount::" + sidx + "::" + sId) != null;
                    model.addAttribute("liked", liked);
                }
        
                model.addAttribute("salesBoard", salesBoardService.getSalesBoardById(sidx));
        
                return "/guest/salesboarddetail";
            }
        
            // 게시글 삭제 하기
            @GetMapping("/delete")
            public String delete(@RequestParam Long sidx){
                salesBoardService.deletedSalesBored(sidx);
        
                return "redirect:/salesboard";
            }
        
            // 게시글 수정 폼
            @GetMapping("/edit")
            public String editForm(Long sidx, Model model) {
                model.addAttribute("salesBoard", salesBoardService.getSalesBoardById(sidx));
        
                return "member/salesboardeditorform";
            }
        
            // 글쓰기 수정 처리
            @PostMapping("/edit")
            public String edit(HttpServletRequest request, SalesBoardDTO  salesBoardDTO,  @RequestParam("file") MultipartFile file) throws FileNotFoundException {
                
                // 파일 업로드
                if (file != null && !file.isEmpty()) {
                    String oImageName = file.getOriginalFilename();
                    String uploadDir = new File("src/main/resources/static/images").getAbsolutePath(); // 이미지 저장 경로 지정
        
                    File dir = new File(uploadDir);
                    if (!dir.exists()) {
                        dir.mkdir();
                    }
        
                    String sImageName = UUID.randomUUID().toString() + "_" + oImageName;
        
                    File destination = new File(dir, sImageName);
                    try {
                        file.transferTo(destination);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "redirect:/salesboard/write?status=fail";
                    }
        
                    // 파일이 있을 경우에만 DTO에 이미지 설정
                    salesBoardDTO.setOimage(oImageName);
                    salesBoardDTO.setSimage(sImageName);
                } else {
                    // 파일이 없을 때
                    System.out.println("No file uploaded.");
                }
        
                // 지역 데이터
                String sido = request.getParameter("sido1");
                String gugun = request.getParameter("gugun1");
                salesBoardDTO.setRegion(sido + " " + gugun);
        
                // 가격 데이터 default 값 넣기
                Integer price = (salesBoardDTO.getPrice() == null) ? 0 : salesBoardDTO.getPrice();
                salesBoardDTO.setPrice(price);
        
                salesBoardService.updateSalesBored(salesBoardDTO.getSidx(), salesBoardDTO);
                return "redirect:/salesboard/detail?sidx=" + salesBoardDTO.getSidx();
            }
            
            // 좋아요 기능
            @GetMapping("/like")
            @ResponseBody
            public ResponseEntity<?> like(Long sidx, HttpServletRequest request) {
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                if (sId.equals("anonymousUser")) {
                    // 비로그인 사용자일 경우 에러 응답
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("로그인 후 좋아요를 눌러주세요.");
                }
                salesBoardService.updateLikeCount(sidx, request);
        
                return ResponseEntity.ok("좋아요가 추가되었습니다.");
            }
        
            // 다운로드
            @GetMapping("/download")
            public ResponseEntity<Object> download(String simage) {
                String path = new File("src/main/resources/static/images").getAbsolutePath() +"/" + simage;
                System.out.println(path);
        
                try {
                    Path filePath = Paths.get(path);
                    Resource resource = new InputStreamResource(Files.newInputStream(filePath)); // 파일 resource 얻기
        
                    File file = new File(path);
        
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentDisposition(ContentDisposition.builder("attachment").filename(file.getName()).build());  // 다운로드 되거나 로컬에 저장되는 용도로 쓰이는지를 알려주는 헤더
        
                    return new ResponseEntity<Object>(resource, headers, HttpStatus.OK);
                } catch(Exception e) {
                    return new ResponseEntity<Object>(null, HttpStatus.CONFLICT);
                }
            }
        
            // 좋아요 취소
            @PostMapping("/updateDownloadCount")
            public ResponseEntity<Void> updateDownloadCount(@RequestParam("sidx") Long sidx) {
                salesBoardService.updateDownCount(sidx);
                return ResponseEntity.ok().build();
            }
        
        
        
        }
    </details>

    <details>
    <summary>SalesBoardRepository.java</summary>

        @Repository
        public interface SalesBoardRepository extends JpaRepository<SalesBoard, Long> {
        
            @Modifying
            @Query(value = "update SALESBOARD set sview_count=sview_count+1 where sidx=:sidx", nativeQuery = true)
            int viewCount(@Param("sidx") Long sidx); // 조회수 증가
        
            @Modifying
            @Query(value = "update SALESBOARD set slike_count=slike_count+1 where sidx=:sidx", nativeQuery = true)
            int likeCount(@Param("sidx") Long sidx); // 좋아요 증가
        
            @Modifying
            @Query(value = "update SALESBOARD set slike_count=slike_count-1 where sidx=:sidx", nativeQuery = true)
            int unlikeCount(@Param("sidx") Long sidx); // 좋아요 감소
        
            @Modifying
            @Query(value = "update SALESBOARD set sdown_count=sdown_count+1 where sidx=:sidx", nativeQuery = true)
            int downCount(@Param("sidx") Long sidx); // 다운로드 수 증가
        
            Page<SalesBoard> findByTitleContaining(String title, Pageable pageable);
        
            Page<SalesBoard> findByContentContaining(String content, Pageable pageable);
        
            Page<SalesBoard> findByNickContaining(String nick, Pageable pageable);
        
            Page<SalesBoard> findByBooktitleContaining(String booktitle, Pageable pageable);
        
            Page<SalesBoard> findByAuthorContaining(String author, Pageable pageable);
        
            Page<SalesBoard> findByPublisherContaining(String publisher, Pageable pageable);
        }
    </details>

    <details>
    <summary>SalesBoardRepository.java</summary>

        @Service
        @RequiredArgsConstructor
        @Transactional
        public class SalesBoardService {
            @Autowired
            private SalesBoardRepository salesBoardRepository;
            @Autowired
            private SalesBoardMapper salesBoardMapper;
            @Autowired
            private RedisUtil redisUtil;
            
            // 게시글 보기
            public Page<SalesBoardDTO> getAllSalesBoards(int page, String searchField, String searchWord){
                Pageable pageable = PageRequest.of(page, 10, Sort.by(Sort.Direction.DESC, "sidx"));
                Page<SalesBoard> salesBoardPage;
        
                if(searchField != null && searchWord != null && !searchWord.isEmpty())
                {
                    switch (searchField)
                    {
                        case "title":
                            salesBoardPage = salesBoardRepository.findByTitleContaining(searchWord, pageable);
                            break;
                        case "content":
                            salesBoardPage = salesBoardRepository.findByContentContaining(searchWord, pageable);
                            break;
                        case "nick":
                            salesBoardPage = salesBoardRepository.findByNickContaining(searchWord, pageable);
                            break;
                        case "booktitle":
                            salesBoardPage = salesBoardRepository.findByBooktitleContaining(searchWord, pageable);
                            break;
                        case "author":
                            salesBoardPage = salesBoardRepository.findByAuthorContaining(searchWord, pageable);
                            break;
                        case "publisher":
                            salesBoardPage = salesBoardRepository.findByPublisherContaining(searchWord, pageable);
                            break;
                        default:
                            salesBoardPage = salesBoardRepository.findAll(pageable);
                    }
                }else{
                    salesBoardPage = salesBoardRepository.findAll(pageable);
                }
        
                List<SalesBoardDTO> dtoList = salesBoardMapper.toDtoList(salesBoardPage.getContent());
        
                return new PageImpl<>(dtoList, salesBoardPage.getPageable(), salesBoardPage.getTotalElements());
            }
        
            // 게시글 작성하기
            public void createSalesBoard(SalesBoardDTO salesBoardDTO) {
                SalesBoard salesBoard = salesBoardMapper.toEntity(salesBoardDTO);
        
                salesBoardRepository.save(salesBoard);
            }
        
            // 게시글 상세 조회
            public SalesBoardDTO getSalesBoardById(Long sidx) {
                SalesBoard salesBoard = salesBoardRepository.findById(sidx).get();
        
                return salesBoardMapper.toDto(salesBoard);
            }
        
            // 게시글 삭제하기
            public void deletedSalesBored(Long sidx){
                salesBoardRepository.deleteById(sidx);
            }
        
            // 게시글 수정하기
            public void updateSalesBored(Long sidx, SalesBoardDTO salesBoardDTO){
                SalesBoard salesBoard = salesBoardRepository.findById(sidx).get();
                SalesBoardDTO originalDTO = salesBoardMapper.toDto(salesBoard);
        
                originalDTO.setSidx(sidx);
                originalDTO.setNick(salesBoardDTO.getNick());
                originalDTO.setClassification(salesBoardDTO.getClassification());
                originalDTO.setRegion(salesBoardDTO.getRegion());
                originalDTO.setTitle(salesBoardDTO.getTitle());
                originalDTO.setBooktitle(salesBoardDTO.getBooktitle());
                originalDTO.setAuthor(salesBoardDTO.getAuthor());
                originalDTO.setPublisher(salesBoardDTO.getPublisher());
                originalDTO.setPrice(salesBoardDTO.getPrice());
                originalDTO.setContent(salesBoardDTO.getContent());
                originalDTO.setUpdateDate(salesBoardDTO.getCreateDate());
        
                
                //  이미지 파일 변경되었을때만 업데이트 되도록
                if (salesBoardDTO.getOimage() != null && !salesBoardDTO.getOimage().isEmpty()) {
                    originalDTO.setOimage(salesBoardDTO.getOimage());
                }
        
                if (salesBoardDTO.getSimage() != null && !salesBoardDTO.getSimage().isEmpty()) {
                    originalDTO.setSimage(salesBoardDTO.getSimage());
                }
        
                salesBoardRepository.save(salesBoardMapper.toEntity(originalDTO));
            }
        
            // 거래게시판 조회수
            public void updateViewCount(Long sidx, HttpServletRequest req)
            {
                String userId = req.getRemoteAddr(); // IP 주소 가져오기
                String key;
        
                // 사용자의 로그인 상태 확인
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                if (sId.equals("anonymousUser")) {
                    // 비로그인 사용자일 경우 IP로 키 생성
                    key = "viewCount::" + sidx + "::" + userId;
                } else {
                    // 로그인 사용자일 경우 ID로 키 생성
                    key = "viewCount::" + sidx + "::" + sId;
                }
        
                // Redis에 데이터가 없을 경우만 조회 수 증가
                if (redisUtil.getData(key) == null) {
                    salesBoardRepository.viewCount(sidx); // 조회 수 증가
        
                    redisUtil.setDataExpire(key, "viewed", 1200); // 유효시간 20분
                }
            }
        
            // 게시글 좋아요
            public void updateLikeCount(Long sidx, HttpServletRequest req)
            {
                String userId = req.getRemoteAddr(); // IP 주소 가져오기
                String key;
        
                // 사용자의 로그인 상태 확인
                String sId = SecurityContextHolder.getContext().getAuthentication().getName();
                if (sId.equals("anonymousUser")) {
                    // 비로그인 사용자일 경우 처리하지 않음 (여기서는 호출되지 않음)
                    return;
                } else {
                    // 로그인 사용자일 경우 ID로 키 생성
                    key = "likeCount::" + sidx + "::" + sId;
                }
        
                // Redis에 데이터가 있는지 확인하여 좋아요 추가 또는 취소
                if (redisUtil.getData(key) == null) {
                    // 좋아요 추가
                    salesBoardRepository.likeCount(sidx); // 좋아요 수 증가
                    redisUtil.setDataExpire(key, "liked", 1200); // 유효시간 20분
                } else {
                    // 좋아요 취소
                    salesBoardRepository.unlikeCount(sidx); // 좋아요 수 감소
                    redisUtil.removeData(key); // Redis에서 제거
                }
            }
        
            // 게시글 좋아요 취소 
            public void updateDownCount(Long sidx)
            {
                salesBoardRepository.downCount(sidx);
            }
        
        }
    </details>

## 주요기능
#### 로그인 
- DB값 검증
- ID찾기, PW찾기
- 계정 잠금
- 인증번호 & 임시 비밀번호 이메일로 발급
- 로그인 시 쿠키(Cookie) 및 세션(Session) 생성

#### 회원가입
- 주소 API 연동
- ID 중복 체크
- 소셜 로그인 (카카오, 네이버, 구글)

#### 마이 페이지
- 주소 API 연동
- 회원 정보 변경
- PW 변경
- 회원 탈퇴

#### 도서 검색
- 카테고리별 리스트 정보 제공
- 도서 상세 정보 조회
- 도서 검색

#### 거래 게시판
- 글 작성, 읽기, 수정, 삭제(CRUD)
- Redis - 조회수 및 좋아요 제한
- 페이징 및 검색
- 파일 업로드 및 다운로드
- 결제 API

#### 문의사항 게시판
- 글 작성, 읽기, 수정, 삭제(CRUD)
- 비밀글 구현
- 관리자일시 답글 작성

#### 쪽지 기능
- 실시간 알람
- 쪽지 목록
- 쪽지 보내기 및 읽기

#### 채팅 상담
- Firebase 연동
- 팝업으로 채팅 입장 확인
- 메세지 전송
- 채팅방 삭제
- 관리자일시 채팅방 리스트

#### 관리자 페이지
- 회원 목록 및 상세정보 확인
- 회원 권한 및 잠금 설정  
