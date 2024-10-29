package com.book.BookProject;

import com.book.BookProject.user.UserDTO;
import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OAuth2SignupController {
    private final HttpSession session;
    private final UserRepository userRepository;

    public OAuth2SignupController(HttpSession session, UserRepository userRepository) {
        this.session = session;
        this.userRepository = userRepository;
    }

    @GetMapping("/SocialLoginSuccess")
    public String handleLoginRedirect() {
        // 세션에서 리다이렉트할 URL 확인
        String redirectUri = (String) session.getAttribute("redirectUri");

        if (redirectUri != null) {
            // 세션에서 redirectUri가 설정되어 있으면 해당 경로로 이동
            return "redirect:" + redirectUri;
        }

        // 기본적으로 메인 페이지로 리다이렉트
        return "redirect:/";
    }

    // 소셜 회원가입 페이지로 이동
    @GetMapping("/guest/SocialSignup")
    public String showSocialSignupForm(Model model) {
        // 세션에서 소셜 사용자 정보 가져오기
        UserEntity socialUser = (UserEntity) session.getAttribute("socialUser");

        if (socialUser != null) {
            // 세션에 있는 정보를 DTO로 변환하여 모델에 전달
            UserDTO userDTO = new UserDTO();
            userDTO.setEmail(socialUser.getEmail());
            userDTO.setSocialProvider(socialUser.getSocialProvider());
            userDTO.setSocialId(socialUser.getSocialId());
            model.addAttribute("userDTO", userDTO);
        } else {
            model.addAttribute("userDTO", new UserDTO());  // 빈 DTO 전달
        }

        return "guest/SocialSignup";  // 소셜 회원가입 페이지로 이동
    }

    // 소셜 회원가입 처리
    @PostMapping("/guest/SocialSignup")
    public String handleSocialSignup(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        // 세션에서 socialUser 정보를 가져옴
        UserEntity socialUser = (UserEntity) session.getAttribute("socialUser");

        if (socialUser != null) {
            // 닉네임이 중복되었는지 확인
            if (userRepository.findByNick(userDTO.getNick()).isPresent()) {
                // 닉네임 중복 시 오류 메시지 전달
                redirectAttributes.addFlashAttribute("errorMessage", "이미 사용 중인 닉네임입니다.");
                return "redirect:/guest/SocialSignup";  // 다시 회원가입 페이지로
            }

            // 소셜 유저 정보 업데이트
            socialUser.setNick(userDTO.getNick());
            socialUser.setName(userDTO.getName());
            socialUser.setPhone(userDTO.getPhone());
            socialUser.setZipcode(userDTO.getZipcode());
            socialUser.setAddress(userDTO.getAddress());
            socialUser.setDetailAddress(userDTO.getDetailAddress());

            // DB에 업데이트된 유저 정보 저장
            userRepository.save(socialUser);

            // 세션에서 socialUser 정보 제거
            session.removeAttribute("socialUser");

            // 회원가입 완료 후 메인 페이지로 리다이렉트
            return "redirect:/";
        }

        // 오류 발생 시 다시 회원가입 페이지로 이동
        return "guest/SocialSignup";
    }
}