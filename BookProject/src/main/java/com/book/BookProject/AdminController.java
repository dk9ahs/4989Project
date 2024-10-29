package com.book.BookProject;

import com.book.BookProject.user.AdminService;
import com.book.BookProject.user.UserEntity;
import com.book.BookProject.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final AdminService adminService;


    public AdminController(UserRepository userRepository, AdminService adminService) {
        this.userRepository = userRepository;
        this.adminService = adminService;
    }

    // 전체 회원 목록 조회 및 검색 기능 추가
    @GetMapping("/list")
    public String getAllUsers(
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "searchWord", required = false) String searchWord,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        int pageSize = 10;
        Page<UserEntity> users;

        if (searchField != null && searchWord != null && !searchWord.isEmpty()) {
            // 검색 필드에 따라 다른 검색 로직 적용
            if (searchField.equals("id")) {
                users = userRepository.findByIdContaining(searchWord, PageRequest.of(page, pageSize));
            } else if (searchField.equals("name")) {
                users = userRepository.findByNameContaining(searchWord, PageRequest.of(page, pageSize));
            } else {
                users = userRepository.findAll(PageRequest.of(page, pageSize));  // 전체 회원
            }
        } else {
            users = userRepository.findAll(PageRequest.of(page, pageSize));
        }

        // 페이지 관련 정보 설정
        Map<String, Object> maps = new HashMap<>();
        maps.put("totalCount", users.getTotalElements());
        maps.put("pageNum", page + 1);
        maps.put("pageSize", pageSize);

        model.addAttribute("lists", users.getContent());
        model.addAttribute("maps", maps);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("currentPage", page);

        return "admin/UserList";
    }

    // 일반 회원 목록 조회
    @GetMapping("/localList")
    public String getLocalUsers(Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 10;
        Page<UserEntity> localUsers = userRepository.findBySocialProviderIsNull(PageRequest.of(page, pageSize));

        Map<String, Object> maps = new HashMap<>();
        maps.put("totalCount", localUsers.getTotalElements());  // 총 회원 수
        maps.put("pageNum", page + 1);  // 현재 페이지 번호
        maps.put("pageSize", pageSize);  // 한 페이지에 보여줄 회원 수

        model.addAttribute("lists", localUsers.getContent());
        model.addAttribute("maps", maps);  // maps 객체를 모델에 추가
        model.addAttribute("totalPages", localUsers.getTotalPages());
        model.addAttribute("currentPage", page);

        return "admin/UserList";
    }

    // 소셜 회원 목록 조회
    @GetMapping("/socialList")
    public String getSocialUsers(Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 10;
        Page<UserEntity> socialUsers = userRepository.findBySocialProviderIsNotNull(PageRequest.of(page, pageSize));

        Map<String, Object> maps = new HashMap<>();
        maps.put("totalCount", socialUsers.getTotalElements());  // 총 회원 수
        maps.put("pageNum", page + 1);  // 현재 페이지 번호
        maps.put("pageSize", pageSize);  // 한 페이지에 보여줄 회원 수

        model.addAttribute("lists", socialUsers.getContent());
        model.addAttribute("maps", maps);  // maps 객체를 모델에 추가
        model.addAttribute("totalPages", socialUsers.getTotalPages());
        model.addAttribute("currentPage", page);

        return "admin/UserList";
    }

    // 잠금 계정 회원 목록 조회
    @GetMapping("/lockList")
    public String getLockedUsers(Model model, @RequestParam(defaultValue = "0") int page) {
        int pageSize = 10;
        Page<UserEntity> lockedUsers = userRepository.findByAccountLocked(1, PageRequest.of(page, pageSize));

        Map<String, Object> maps = new HashMap<>();
        maps.put("totalCount", lockedUsers.getTotalElements());  // 총 회원 수
        maps.put("pageNum", page + 1);  // 현재 페이지 번호
        maps.put("pageSize", pageSize);  // 한 페이지에 보여줄 회원 수

        model.addAttribute("lists", lockedUsers.getContent());
        model.addAttribute("maps", maps);  // maps 객체를 모델에 추가
        model.addAttribute("totalPages", lockedUsers.getTotalPages());
        model.addAttribute("currentPage", page);

        return "admin/UserList";
    }
    // 특정 회원 정보 조회
    @GetMapping("/userdetails")
    public String getUserDetails(@RequestParam("id") String id, Model model) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 회원이 없습니다: " + id));
        model.addAttribute("user", user);
        return "admin/UserDetails";  // 회원 정보 페이지로 이동
    }

    // 계정 잠금 처리
    @PostMapping("/lockAccount")
    public String lockAccount(@RequestParam("id") String id) {
        adminService.lockAccount(id);  // 서비스에서 트랜잭션 처리
        return "redirect:/admin/userdetails?id=" + id;
    }

    // 계정 잠금 해제 처리
    @PostMapping("/unlockAccount")
    public String unlockAccount(@RequestParam("id") String id) {
        adminService.unlockAccount(id);  // 서비스에서 트랜잭션 처리
        return "redirect:/admin/userdetails?id=" + id;
    }

    // 회원 강제 탈퇴 처리
    @PostMapping("/deleteUser")
    public String deleteUser(@RequestParam("id") String id) {
        adminService.deleteUser(id);  // 서비스에서 트랜잭션 처리
        return "redirect:/admin/list";
    }
}