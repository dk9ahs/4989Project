package com.book.BookProject.message.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//웹소켓 설정을 위한 자동 빈생성, 소켓활성화, 인수생성자를 위한 어노테이션을 부착한다.
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

