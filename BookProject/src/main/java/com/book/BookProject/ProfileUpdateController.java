package com.book.BookProject;

import com.book.BookProject.user.ProfileUpdateService;
import com.book.BookProject.user.UserDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
@RequestMapping("/member")
public class ProfileUpdateController {

    private final ProfileUpdateService profileUpdateService;

    public ProfileUpdateController(ProfileUpdateService profileUpdateService) {
        this.profileUpdateService = profileUpdateService;
    }

    // 회원 정보 수정 폼 표시
    @GetMapping("Update")
    public String showProfileUpdateForm(Model model, Principal principal) {
        UserDTO userDTO = profileUpdateService.getUserByUsername(principal.getName());
        model.addAttribute("userDTO", userDTO);
        return "member/profileUpdate";
    }

    // 일반 회원 정보 수정 처리
    @PostMapping("Update")
    public String updateProfile(@Valid @ModelAttribute("userDTO") UserDTO userDTO, BindingResult result,
                                Model model, Principal principal,
                                HttpServletRequest request, HttpServletResponse response,
                                RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "member/profileUpdate";
        }

        try {
            boolean isSocialUser = profileUpdateService.isSocialUser(principal.getName());

            // 소셜 로그인 사용자는 이메일과 비밀번호를 수정할 수 없도록 처리
            if (isSocialUser) {
                if (!userDTO.getEmail().equals(profileUpdateService.getUserByUsername(principal.getName()).getEmail())) {
                    model.addAttribute("errorMessage", "소셜 로그인 사용자는 이메일을 수정할 수 없습니다.");
                    return "member/profileUpdate";
                }
                if (userDTO.getPwd() != null && !userDTO.getPwd().isEmpty()) {
                    model.addAttribute("errorMessage", "소셜 로그인 사용자는 비밀번호를 수정할 수 없습니다.");
                    return "member/profileUpdate";
                }
            }

            profileUpdateService.updateUserProfile(userDTO, principal.getName());

            request.getSession().invalidate();
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, SecurityContextHolder.getContext().getAuthentication());

            // 성공 메시지 전달
            redirectAttributes.addFlashAttribute("updateSuccess", "회원정보가 성공적으로 수정되었습니다. 다시 로그인 해주세요.");

            return "redirect:/login?logout";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "오류가 발생했습니다: " + e.getMessage());
            return "member/profileUpdate";
        }
    }

    // 소셜 로그인 회원 정보 수정 처리
    @PostMapping("/socialUpdate")
    public String updateSocialProfile(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                                      BindingResult result,
                                      Model model, Principal principal,
                                      RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "member/profileUpdate";
        }

        try {
            boolean isSocialUser = profileUpdateService.isSocialUser(principal.getName());

            if (!isSocialUser) {
                model.addAttribute("errorMessage", "일반 회원은 소셜 회원 정보 수정 페이지에 접근할 수 없습니다.");
                return "member/profileUpdate";
            }

            profileUpdateService.updateUserProfile(userDTO, principal.getName());
            // 성공 메시지 전달
            redirectAttributes.addFlashAttribute("updateSuccess", "회원정보가 성공적으로 수정되었습니다. 다시 로그인 해주세요.");
            return "redirect:/member/socialUpdate";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "오류가 발생했습니다: " + e.getMessage());
            return "member/profileUpdate";
        }
    }

    @PostMapping("/withdraw")
    public String withdrawMember(Principal principal, HttpServletRequest request, HttpServletResponse response, RedirectAttributes redirectAttributes) {
        profileUpdateService.deleteUserById(principal.getName());

        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());

        redirectAttributes.addFlashAttribute("withdrawSuccess", "회원탈퇴가 완료되었습니다.");
        return "redirect:/login?logout=true";
    }
}