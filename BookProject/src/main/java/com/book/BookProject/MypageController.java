package com.book.BookProject;

import com.book.BookProject.order.Order;
import com.book.BookProject.order.OrderService;
import com.book.BookProject.salesboard.MemberService;
import com.book.BookProject.user.UserEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

@RequestMapping("/mypage")
@Controller
public class MypageController {

    private OrderService orderService;
    private MemberService memberService;

    public MypageController(OrderService orderService, MemberService memberService) {
        this.orderService = orderService;
        this.memberService = memberService;
    }

    @GetMapping
    public String mypage() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return "/login";
        } else {
            return "/member/mypage";
        }
    }

    @GetMapping("/order")
    public String orderlist(Model model) {
        String sId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = memberService.findUserById(sId);

        List<Order> orders = orderService.getOrdersByUser(user.getId());

        // 주문 목록에서 각각의 가격을 포맷팅하여 새로운 속성으로 추가
        orders.forEach(order -> {
            String formattedPrice = formatPrice(order.getTotalAmount());
            order.setFormattedTotalAmount(formattedPrice); // 새로 추가한 필드에 저장
        });

        model.addAttribute("orders", orders);

        return "member/order/orderList";
    }

    // 가격을 3자리마다 콤마 추가 및 "원"을 붙이는 메서드
    private String formatPrice(Double amount) {
        if (amount == null) {
            return "가격 정보 없음";
        }
        NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.KOREA);
        return numberFormat.format(amount) + " 원";
    }
}