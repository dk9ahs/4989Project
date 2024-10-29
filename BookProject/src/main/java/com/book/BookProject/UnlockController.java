package com.book.BookProject;

import com.book.BookProject.security.UserServiceImpl;
import com.book.BookProject.sms.CoolSmsService;
import com.book.BookProject.sms.VerificationCodeService;
import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import net.nurigo.java_sdk.exceptions.CoolsmsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UnlockController {

    private final UserRepository userRepository;
    private final CoolSmsService coolSmsService;
    private final UserServiceImpl userServiceImpl;
    private final VerificationCodeService verificationCodeService;

    @Autowired
    public UnlockController(UserRepository userRepository, CoolSmsService coolSmsService, UserServiceImpl userServiceImpl, VerificationCodeService verificationCodeService) {
        this.userRepository = userRepository;
        this.coolSmsService = coolSmsService;
        this.userServiceImpl = userServiceImpl;
        this.verificationCodeService = verificationCodeService;
    }

    @PostMapping("/guest/unlock")
    public String unlockUserAccount(@RequestParam("username") String username,
                                    @RequestParam("phone") String phone,
                                    @RequestParam(value = "verificationCode", required = false) String verificationCode,
                                    RedirectAttributes redirectAttributes) {
        UserEntity user = userRepository.findById(username).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "존재하지 않는 사용자입니다.");
            return "redirect:/guest/unlock";
        }

        if (!user.getPhone().equals(phone)) {
            redirectAttributes.addFlashAttribute("errorMessage", "입력한 전화번호가 등록된 번호와 일치하지 않습니다.");
            return "redirect:/guest/unlock";
        }

        if (verificationCode != null) {
            String storedCode = verificationCodeService.getCode(username);

            if (storedCode != null && storedCode.equals(verificationCode)) {
                userServiceImpl.unlockAccount(username);
                redirectAttributes.addFlashAttribute("successMessage", "계정 잠금이 성공적으로 해제되었습니다.");
                verificationCodeService.removeCode(username);
                return "redirect:/login";
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "잘못된 인증번호입니다.");
                return "redirect:/guest/unlock";
            }
        }

        try {
            String generatedCode = coolSmsService.sendSms(phone);
            verificationCodeService.saveCode(username, generatedCode);
            redirectAttributes.addFlashAttribute("successMessage", "인증번호가 발송되었습니다.");
        } catch (CoolsmsException e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "SMS 전송에 실패했습니다.");
        }

        return "redirect:/guest/unlock";
    }
}
