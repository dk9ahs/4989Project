package com.book.BookProject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
public class BookController {

    private final String API_URL = "http://www.aladin.co.kr/ttb/api/ItemList.aspx";
//    private final String TTB_KEY = "ttbtle651621001"; // 지훈형님 키
    private final String TTB_KEY = "ttbooo00110134001"; // 다빈님 키
//    private final String TTB_KEY = "ttblckdrbs1419006"; // 창균 키


    @GetMapping("/bookList")
    public String showBookListPage(@RequestParam String text,
                                   @RequestParam(defaultValue = "Title") String category,
                                   @RequestParam(defaultValue = "1") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   Model model) {
        // 검색 파라미터를 모델에 추가하여 뷰에서 사용
        model.addAttribute("text", text);
        model.addAttribute("category", category);
        model.addAttribute("page", page);
        model.addAttribute("size", size);

        // guest/bookList 템플릿을 반환하여 페이지를 렌더링
        return "guest/bookList";
    }

    @GetMapping("/bookdetail")
    public String getBookDetailPage(@RequestParam("isbn") String isbn, Model model, Authentication authentication) {

        // 로그인 여부를 확인하여 모델에 추가
        boolean isAuthenticated = authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal());
        model.addAttribute("isAuthenticated", isAuthenticated);

        // ISBN 값을 모델에 추가하여 뷰에서 사용
        model.addAttribute("isbn", isbn);
        return "guest/bookdetail";
    }

    // 카테고리별 책 리스트를 보여주는 기능 추가
    @GetMapping("/category")
    public String showCategoryBooks(@RequestParam("categoryId") String categoryId,
                                    @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int size,
                                    Model model) {
        RestTemplate restTemplate = new RestTemplate();
        String apiUrl = API_URL + "?ttbkey=" + TTB_KEY
                + "&QueryType=Bestseller&CategoryId=" + categoryId
                + "&start=" + ((page - 1) * size + 1)
                + "&MaxResults=" + size
                + "&output=js&Version=20131101";

        try {
            // API 응답을 받아서 처리
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(apiUrl, String.class);
            String response = responseEntity.getBody();

            // 응답 상태 코드 확인
            if (!responseEntity.getStatusCode().is2xxSuccessful()) {
                model.addAttribute("error", "API 호출 실패, 상태 코드: " + responseEntity.getStatusCodeValue());
                return "error";
            }

            // 응답이 JSON 형식인지 확인
            if (response == null || (!response.trim().startsWith("{") && !response.trim().startsWith("["))) {
                model.addAttribute("error", "API 응답이 올바른 JSON 형식이 아닙니다.");
                return "error";
            }

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode jsonNode = objectMapper.readTree(response);

            // 응답 데이터가 예상한 형태인지 확인
            if (!jsonNode.has("item") || !jsonNode.path("item").isArray()) {
                model.addAttribute("error", "API 응답 데이터 형식이 올바르지 않습니다.");
                return "error";
            }

            model.addAttribute("books", jsonNode);
            model.addAttribute("categoryId", categoryId);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
        } catch (Exception e) {
            model.addAttribute("error", "API 호출에 실패했습니다: " + e.getMessage());
            return "error";
        }

        return "guest/bookCategory";
    }

    @GetMapping("/bestseller")
    public String bestseller() {
        return "guest/bestSellerList";
    }
}