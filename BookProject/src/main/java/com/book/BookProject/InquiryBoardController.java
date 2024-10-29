package com.book.BookProject;

import com.book.BookProject.inquiryboard.InquiryBoard;
import com.book.BookProject.inquiryboard.InquiryBoardDTO;
import com.book.BookProject.inquiryboard.InquiryBoardService;
import com.book.BookProject.salesboard.Redis.RedisUtil;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequestMapping("/inquiryboard")
@Controller
public class InquiryBoardController
{
    @Autowired
    InquiryBoardService inquiryBoardService;
    @Autowired
    ServletContext context;
    @Autowired
    RedisUtil redisUtil;

    // 문의게시판 리스트
    @GetMapping
    public String inquiryBoard(Model model,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String searchField,
                               @RequestParam(required = false) String searchWord)
    {
        Page<InquiryBoardDTO> listPage = inquiryBoardService.inquiryBoardList(page - 1, searchField, searchWord);

        // 비밀 글 일 때 로그인 된 닉네임과 비교 하기 위해
        String id = SecurityContextHolder.getContext().getAuthentication().getName();

        if (id.equals("anonymousUser"))
        {
            model.addAttribute("loginNick", "Guest");
        } else {
            String nick = inquiryBoardService.findNickById(id);
            model.addAttribute("loginNick", nick);
        }

        long totalCount = listPage.getTotalElements();
        int totalPage = listPage.getTotalPages();
        int currentGroup = (page - 1) / 5; // 현재 그룹 (0부터 시작)
        int pageSize = listPage.getSize();
        // 리스트
        model.addAttribute("list", listPage.getContent());
        // 페이지
        model.addAttribute("totalPage", totalPage); // 총 페이지
        model.addAttribute("currentPage", page); // 현재 페이지 추가
        model.addAttribute("currentGroup", currentGroup);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("pageSize", pageSize);
        // 검색
        model.addAttribute("searchField", searchField); // 검색필드
        model.addAttribute("searchWord", searchWord); // 검색어

        return "guest/InquiryBoard";
    }

    // 문의게시판 상세보기
    @GetMapping("/view")
    public  String inquiryBoardView(Model model, Long qidx)
    {
        inquiryBoardService.inquiryBoardViewCount(qidx); // 제목 클릭 시 조회수 증가

        InquiryBoardDTO view = inquiryBoardService.inquiryBoardView(qidx);

        model.addAttribute("view", view);

        return "/member/InquiryBoardView";
    }

    // 문의게시판 글 작성 폼
    @GetMapping("/writeform")
    public  String inquiryBoardWriteForm(Model model)
    {
        // 글 작성 시 작성자에 로그인 한 사람의 닉네임 설정 하기 위해
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        // id로 닉네임 찾기
        String nick = inquiryBoardService.findNickById(id);

        model.addAttribute("nick", nick);

        return "member/InquiryBoardWriteForm";
    }

    // 문의게시판 글 작성
    @PostMapping("/write")
    public String inquiryBoardWrite(InquiryBoardDTO inquiryBoardDTO,
                                    @RequestParam("file") MultipartFile file) throws FileNotFoundException
    {
        // 파일 업로드
        if(file != null && !file.isEmpty())
        {
            String oFileName = file.getOriginalFilename();
            String uploadDir = new File("src/main/resources/static/images").getAbsolutePath(); // 이미지 저장 경로 지정
            System.out.println(uploadDir);

            File dir = new File(uploadDir);
            if(!dir.exists())
            {
                dir.mkdir();
            }

            String sFileName = UUID.randomUUID().toString() + "_" + oFileName;

            File destination = new File(dir, sFileName);
            try {
                file.transferTo(destination);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/inquirvboard/write?status=fail";
            }

            // 파일 있을 경우에만 DTO에 이미지 설정
            inquiryBoardDTO.setOfile(oFileName);
            inquiryBoardDTO.setSfile(sFileName);
        } else
        {
            // 파일이 없을 떄
            System.out.println("No file uploaded.");
        }

        inquiryBoardService.inquiryBoardWrite(inquiryBoardDTO);

        return "redirect:/inquiryboard";
    }

    // 문의게시판 글 수정 폼
    @GetMapping("/editform")
    public  String inquiryBoardEditorForm(Model model, Long qidx)
    {
        InquiryBoardDTO inquiryBoard = inquiryBoardService.inquiryBoardView(qidx);
        model.addAttribute("inquiryBoard", inquiryBoard);

        return "/member/InquiryBoardEditorForm";
    }

    // 문의게시판 글 수정
    @PostMapping("/edit")
    public  String inquiryBoardEditor(HttpServletRequest request,
                                      InquiryBoardDTO inquiryBoardDTO,
                                      @RequestParam("file") MultipartFile file) throws FileNotFoundException
    {
        // 파일 업로드
        if(file != null && !file.isEmpty())
        {
            String oFileName = file.getOriginalFilename();
            String uploadDir = request.getSession().getServletContext().getRealPath("/"); // src/main/webapp에 저장

            File dir = new File(uploadDir);
            if(!dir.exists())
            {
                dir.mkdir();
            }

            String sFileName = UUID.randomUUID().toString() + "_" + oFileName;

            File destination = new File(dir, sFileName);
            try {
                file.transferTo(destination);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/inquirvboard/write?status=fail";
            }

            // 파일 있을 경우에만 DTO에 이미지 설정
            inquiryBoardDTO.setOfile(oFileName);
            inquiryBoardDTO.setSfile(sFileName);
        } else
        {
            // 파일이 없을 떄
            System.out.println("No file uploaded.");
        }
        inquiryBoardService.inquiryBoardEditor(inquiryBoardDTO.getQidx(), inquiryBoardDTO);

        return "redirect:/inquiryboard/view?qidx=" + inquiryBoardDTO.getQidx();
    }

    // 문의게시판 글 삭제
    @GetMapping("/delete")
    public  String inquiryBoardDelete(Long qidx)
    {
        inquiryBoardService.inquiryBoardDelete(qidx);

        return "redirect:/inquiryboard";
    }

    // 문의게시판 답글 작성 폼
    @GetMapping("/replywriteform")
    public  String inquiryBoardReplyWriteForm(Model model, Long qidx)
    {
        InquiryBoardDTO inquiryBoardDTO = inquiryBoardService.inquiryBoardView(qidx);
        model.addAttribute("inquiryBoardDTO", inquiryBoardDTO);

        // 글 작성 시 작성자에 로그인 한 사람의 닉네임 설정 하기 위해
        String id = SecurityContextHolder.getContext().getAuthentication().getName();
        // id로 닉네임 찾기
        String adminNick = inquiryBoardService.findNickById(id);

        model.addAttribute("adminNick", adminNick);

        return "admin/InquiryBoardReplyWriteForm";
    }

    // 문의게시판 답글 작성
    @PostMapping("/replywrite")
    public  String inquiryBoardReplyWrite(HttpServletRequest request,
                                          InquiryBoardDTO inquiryBoardDTO,
                                          Long qidx,
                                          @RequestParam("file") MultipartFile file) throws FileNotFoundException
    {
        // 부모 글 dto
        InquiryBoardDTO boardDTO = inquiryBoardService.inquiryBoardView(qidx);
        inquiryBoardDTO.setQidx(null);  // 새로 생성될 글이므로 null로 설정
        inquiryBoardDTO.setOrigin(boardDTO.getOrigin());
        inquiryBoardDTO.setGroup(boardDTO.getGroup() + 1);
        inquiryBoardDTO.setLayer(boardDTO.getLayer() + 1);

        inquiryBoardService.updateResponseStatus(boardDTO.getOrigin());

        // 파일 업로드
        if(file != null && !file.isEmpty())
        {
            String oFileName = file.getOriginalFilename();
            String uploadDir = request.getSession().getServletContext().getRealPath("/"); // src/main/webapp에 저장

            File dir = new File(uploadDir);
            if(!dir.exists())
            {
                dir.mkdir();
            }

            String sFileName = UUID.randomUUID().toString() + "_" + oFileName;

            File destination = new File(dir, sFileName);
            try {
                file.transferTo(destination);
            } catch (IOException e) {
                e.printStackTrace();
                return "redirect:/inquirvboard/replywrite?status=fail";
            }

            // 파일 있을 경우에만 DTO에 이미지 설정
            inquiryBoardDTO.setOfile(oFileName);
            inquiryBoardDTO.setSfile(sFileName);
        } else
        {
            // 파일이 없을 떄
            System.out.println("No file uploaded.");
        }

        inquiryBoardService.inquiryBoardWrite(inquiryBoardDTO);

        return "redirect:../inquiryboard";
    }

    // 문의게시판 글 비밀번호 창
    @GetMapping("/pass")
    public  String inquiryBoardPass(Model model, Long qidx)
    {
        InquiryBoardDTO pass = inquiryBoardService.inquiryBoardView(qidx);

        model.addAttribute("pass", pass.getPass());
        model.addAttribute("qidx", qidx);

        return "member/InquiryBoardPass";
    }

    @PostMapping("/updateResponse")
    public String updateResponse(@RequestParam Long qidx, @RequestParam Long originNo) {
        inquiryBoardService.updateResponseStatus(originNo);
        return "redirect:/inquiryboard";  // 업데이트 후 게시글 리스트로 리다이렉트
    }
}
