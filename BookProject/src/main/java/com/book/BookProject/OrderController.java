package com.book.BookProject;

import com.book.BookProject.order.Order;
import com.book.BookProject.order.OrderService;
import com.book.BookProject.salesboard.MemberService;
import com.book.BookProject.salesboard.SalesBoardService;
import com.book.BookProject.user.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;
    private final MemberService memberService;
    private final SalesBoardService salesBoardService;

    public OrderController(OrderService orderService, MemberService memberService, SalesBoardService salesBoardService) {
        this.orderService = orderService;
        this.memberService = memberService;
        this.salesBoardService = salesBoardService;
    }

    // 주문 생성 페이지로 이동 (로그인된 회원 정보를 전달)
    @GetMapping("/create")
    public String showCreateOrderPage(Model model, HttpServletRequest request) {
        // SecurityContextHolder를 사용해 로그인된 사용자 ID를 가져옴
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = memberService.findUserById(sId);  // `UserEntity` 객체를 반환

        model.addAttribute("user", user);  // 사용자 정보 전달
        model.addAttribute("order", new Order());  // 빈 주문 객체 추가

        return "/member/order/orderCreate";  // 주문 생성 페이지로 이동
    }

    // 주문 저장
    @PostMapping("/saveOrder")
    public ResponseEntity<Map<String, Object>> saveOrder(@RequestBody Map<String, String> orderData, @AuthenticationPrincipal UserDetails userDetails, HttpServletRequest request) {

        try {
            // SecurityContextHolder를 사용해 로그인된 사용자 ID를 가져옴
            String sId = SecurityContextHolder.getContext().getAuthentication().getName();
            UserEntity user = memberService.findUserById(sId);  // `UserEntity` 객체를 반환

            // 주문 정보 생성
            Order order = Order.builder()
                    .member(user)  // 주문자 정보 설정
                    .merchantUid(String.valueOf(System.currentTimeMillis()))  // 고유한 주문 번호를 String으로 변환하여 설정
                    .orderDate(LocalDateTime.now())
                    .status("ORDERED")  // 기본 주문 상태 설정
                    .totalAmount(Double.parseDouble(orderData.get("amount")))  // 결제 금액 설정
                    .shippingAddress(orderData.get("buyerAddr"))  // 배송 주소 설정
                    .detailAddress(orderData.get("buyerDetailAddr"))  // 배송 주소 설정
                    .recipientName(orderData.get("buyerName"))  // 수령인 이름 설정
                    .recipientPhone(orderData.get("buyerTel"))  // 수령인 연락처 설정
                    .paymentMethod("CARD")  // 결제 방법 설정
                    .paymentStatus("PAID")  // 결제 상태 설정
                    .bookTitle(orderData.get("bookTitle"))  // 책 제목 추가
                    .bookAuthor(orderData.get("bookAuthor"))  // 책 저자 추가
                    .bookPublisher(orderData.get("bookPublisher"))  // 출판사 추가
                    .bookImageUrl(orderData.get("bookImageUrl"))  // 책 이미지 URL 추가
                    .build();

            // 주문 정보 저장
            Order savedOrder = orderService.createOrder(order);

            // 응답 데이터 생성
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("order", savedOrder);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("success", false, "message", "주문 저장 중 오류 발생"));
        }
    }

    // 유저 주문 조회
    @GetMapping
    public String getAllOrders(Model model) {

        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = memberService.findUserById(sId);  // `UserEntity` 객체를 반환

        // 사용자의 주문 목록을 가져옴
        List<Order> orders = orderService.getOrdersByUser(user.getId());

        model.addAttribute("orders", orderService.getAllOrders());
        return "member/order/orderList";
    }

    // 특정 주문 조회
    @GetMapping("/{orderId}")
    public String getOrder(@PathVariable Long orderId, Model model) {
        Order order = orderService.getOrderById(orderId);
        if (order != null) {
            model.addAttribute("order", order);
            return "member/orderDetail";
        } else {
            return "error";
        }
    }

    // 주문 삭제
    @PostMapping("/{orderId}/delete")
    public String deleteOrder(@PathVariable Long orderId) {
        orderService.deleteOrder(orderId);
        return "redirect:/order";
    }

}