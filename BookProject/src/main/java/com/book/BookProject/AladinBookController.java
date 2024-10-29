package com.book.BookProject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AladinBookController {

    private final String API_URL = "http://www.aladin.co.kr/ttb/api/ItemList.aspx";
    private final String SEARCH_URL = "http://www.aladin.co.kr/ttb/api/ItemSearch.aspx";
    private final String LOOKUP_URL = "http://www.aladin.co.kr/ttb/api/ItemLookUp.aspx";

//    private final String TTB_KEY = "ttbtle651621001"; // 지훈형님 키
    private final String TTB_KEY = "ttbooo00110134001"; // 다빈님 키
//    private final String TTB_KEY = "ttblckdrbs1419006"; // 창균 키

    // 베스트셀러 리스트 가져오기
    @GetMapping("/book") // /book 경로로 매핑
    public ArrayNode getBooks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) throws Exception {
        String apiUrl = API_URL + "?ttbkey=" + TTB_KEY
                + "&QueryType=Bestseller&MaxResults=50&start=1"
                + "&SearchTarget=Book&output=js&Version=20131101&Cover=Big";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        // JSON 응답 처리
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        ArrayNode books = (ArrayNode) rootNode.path("item");

        // 페이지네이션 처리 (페이지당 표시할 책 수에 따라 잘라내기)
        int start = (page - 1) * size;
        int end = Math.min(start + size, books.size());

        ArrayNode paginatedBooks = objectMapper.createArrayNode();
        for (int i = start; i < end; i++) {
            JsonNode book = books.get(i);
            String coverUrl = book.get("cover").asText().trim();
            coverUrl = coverUrl.replaceAll("^\"|\"$", "");  // 앞뒤에 있는 큰따옴표 제거

            // ISBN13 값 가져오기
            String isbn13 = book.get("isbn13").asText();

            // 별점 정보 추가 (ISBN을 이용해 별점 조회)
            String lookupUrl = LOOKUP_URL + "?ttbkey=" + TTB_KEY + "&itemIdType=ISBN13&ItemId=" + isbn13
                    + "&output=js&Version=20131101&OptResult=ratingInfo";

            String ratingResponse = restTemplate.getForObject(lookupUrl, String.class);
            JsonNode ratingNode = objectMapper.readTree(ratingResponse);

            String rating = "N/A"; // 기본 값 설정
            if (ratingNode.path("item").size() > 0 && ratingNode.path("item").get(0).path("subInfo").has("ratingInfo")) {
                rating = ratingNode.path("item").get(0).path("subInfo").path("ratingInfo").path("ratingScore").asText("N/A");
            }

            // 정리된 URL과 별점 정보를 다시 JSON 노드에 반영
            ((ObjectNode) book).put("cover", coverUrl);
            ((ObjectNode) book).put("rating", rating); // 별점 추가

            paginatedBooks.add(book);
        }

        // 페이징된 결과를 반환
        return paginatedBooks;
    }

    // 카테고리별 베스트셀러 리스트 가져오기
    @GetMapping("/api/category")
    public ObjectNode getCategoryBooks(@RequestParam(defaultValue = "1") int page,
                                       @RequestParam(defaultValue = "10") int size,
                                       @RequestParam String categoryId)
            throws Exception {
        // 알라딘 API의 정렬 옵션을 정확하게 전달
        String apiUrl = API_URL + "?ttbkey=" + TTB_KEY
                + "&QueryType=Bestseller&MaxResults=50&start=1"
                + "&SearchTarget=Book&output=js&Version=20131101&Cover=Big"
                + "&CategoryId=" + categoryId;

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        ArrayNode books = (ArrayNode) rootNode.path("item");

        int totalResults = books.size();

        // 페이지네이션 처리
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalResults);

        ArrayNode paginatedBooks = objectMapper.createArrayNode();
        for (int i = start; i < end; i++) {
            JsonNode book = books.get(i);
            String isbn13 = book.path("isbn13").asText();

            // 별점 정보 추가 (ISBN을 이용해 별점 조회)
            String lookupUrl = LOOKUP_URL + "?ttbkey=" + TTB_KEY + "&itemIdType=ISBN13&ItemId=" + isbn13
                    + "&output=js&Version=20131101&OptResult=ratingInfo";

            String ratingResponse = restTemplate.getForObject(lookupUrl, String.class);
            JsonNode ratingNode = objectMapper.readTree(ratingResponse);

            String rating = "N/A"; // 기본 값 설정
            if (ratingNode.path("item").size() > 0 && ratingNode.path("item").get(0).path("subInfo").has("ratingInfo")) {
                rating = ratingNode.path("item").get(0).path("subInfo").path("ratingInfo").path("ratingScore").asText("N/A");
            }

            // 책 정보에 별점 추가
            ((ObjectNode) book).put("rating", rating);
            paginatedBooks.add(book);
        }

        // 전체 결과 개수와 페이징된 책 목록을 반환
        ObjectNode result = objectMapper.createObjectNode();
        result.put("totalResults", totalResults);
        result.set("books", paginatedBooks);

        return result;
    }


    // 신간 리스트 가져오기
    @GetMapping("/newbook") // 신간 리스트 가져오기
    public ArrayNode getNewBooks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) throws Exception {
        String apiUrl = API_URL + "?ttbkey=" + TTB_KEY
                + "&QueryType=ItemNewAll&MaxResults=50&start=1"
                + "&SearchTarget=Book&output=js&Version=20131101&Cover=Big";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        // JSON 응답 처리
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        ArrayNode books = (ArrayNode) rootNode.path("item");

        // 페이지네이션 처리 (페이지당 표시할 책 수에 따라 잘라내기)
        int start = (page - 1) * size;
        int end = Math.min(start + size, books.size());

        ArrayNode paginatedBooks = objectMapper.createArrayNode();
        for (int i = start; i < end; i++) {
            JsonNode book = books.get(i);
            String coverUrl = book.get("cover").asText().trim();
            coverUrl = coverUrl.replaceAll("^\"|\"$", "");  // 앞뒤에 있는 큰따옴표 제거
            ((ObjectNode) book).put("cover", coverUrl);
            paginatedBooks.add(book);
        }

        // 페이징된 결과를 반환
        return paginatedBooks;
    }

    // '주목할만한 신간 리스트' 가져오기
    @GetMapping("/notablebooks") // 주목할만한 신간 리스트 가져오기
    public ArrayNode getNotableBooks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) throws Exception {
        String apiUrl = API_URL + "?ttbkey=" + TTB_KEY
                + "&QueryType=ItemNewSpecial&MaxResults=50&start=1"
                + "&SearchTarget=Book&output=js&Version=20131101&Cover=Big";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        // JSON 응답 처리
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        ArrayNode books = (ArrayNode) rootNode.path("item");

        // 페이지네이션 처리 (페이지당 표시할 책 수에 따라 잘라내기)
        int start = (page - 1) * size;
        int end = Math.min(start + size, books.size());

        ArrayNode paginatedBooks = objectMapper.createArrayNode();
        for (int i = start; i < end; i++) {
            JsonNode book = books.get(i);
            String coverUrl = book.get("cover").asText().trim();
            coverUrl = coverUrl.replaceAll("^\"|\"$", "");  // 앞뒤에 있는 큰따옴표 제거

            // ISBN13 값 가져오기
            String isbn13 = book.get("isbn13").asText();

            // 별점 정보 추가 (ISBN을 이용해 별점 조회)
            String lookupUrl = LOOKUP_URL + "?ttbkey=" + TTB_KEY + "&itemIdType=ISBN13&ItemId=" + isbn13
                    + "&output=js&Version=20131101&OptResult=ratingInfo";

            String ratingResponse = restTemplate.getForObject(lookupUrl, String.class);
            JsonNode ratingNode = objectMapper.readTree(ratingResponse);

            String rating = "N/A"; // 기본 값 설정
            if (ratingNode.path("item").size() > 0 && ratingNode.path("item").get(0).path("subInfo").has("ratingInfo")) {
                rating = ratingNode.path("item").get(0).path("subInfo").path("ratingInfo").path("ratingScore").asText("N/A");
            }

            // 정리된 URL과 별점 정보를 다시 JSON 노드에 반영
            ((ObjectNode) book).put("cover", coverUrl);
            ((ObjectNode) book).put("rating", rating); // 별점 추가

            paginatedBooks.add(book);
        }

        // 페이징된 결과를 반환
        return paginatedBooks;
    }

    // 블로거 베스트셀러 리스트 가져오기
    @GetMapping("/blogbestbooks") // 블로거 베스트셀러 리스트 가져오기
    public ArrayNode getBloggerBestBooks(@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size) throws Exception {
        String apiUrl = API_URL + "?ttbkey=" + TTB_KEY
                + "&QueryType=BlogBest&MaxResults=50&start=1"
                + "&SearchTarget=Book&output=js&Version=20131101&Cover=Big";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        // JSON 응답 처리
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        ArrayNode books = (ArrayNode) rootNode.path("item");

        // 페이지네이션 처리 (페이지당 표시할 책 수에 따라 잘라내기)
        int start = (page - 1) * size;
        int end = Math.min(start + size, books.size());

        ArrayNode paginatedBooks = objectMapper.createArrayNode();
        for (int i = start; i < end; i++) {
            JsonNode book = books.get(i);
            String coverUrl = book.get("cover").asText().trim();
            coverUrl = coverUrl.replaceAll("^\"|\"$", "");  // 앞뒤에 있는 큰따옴표 제거

            // ISBN13 값 가져오기
            String isbn13 = book.get("isbn13").asText();

            // 별점 정보 추가 (ISBN을 이용해 별점 조회)
            String lookupUrl = LOOKUP_URL + "?ttbkey=" + TTB_KEY + "&itemIdType=ISBN13&ItemId=" + isbn13
                    + "&output=js&Version=20131101&OptResult=ratingInfo";

            String ratingResponse = restTemplate.getForObject(lookupUrl, String.class);
            JsonNode ratingNode = objectMapper.readTree(ratingResponse);

            String rating = "N/A"; // 기본 값 설정
            if (ratingNode.path("item").size() > 0 && ratingNode.path("item").get(0).path("subInfo").has("ratingInfo")) {
                rating = ratingNode.path("item").get(0).path("subInfo").path("ratingInfo").path("ratingScore").asText("N/A");
            }

            // 정리된 URL과 별점 정보를 다시 JSON 노드에 반영
            ((ObjectNode) book).put("cover", coverUrl);
            ((ObjectNode) book).put("rating", rating); // 별점 추가

            paginatedBooks.add(book);
        }

        // 페이징된 결과를 반환
        return paginatedBooks;
    }


    // 특정 책 상세 정보 가져오기
    @GetMapping("/bookdetail/{isbn}")
    public ObjectNode bookDetail(@PathVariable String isbn) throws Exception {
        // API 호출로 책 정보를 가져옴
        String aladinUrl = LOOKUP_URL + "?ttbkey=" + TTB_KEY
                + "&itemIdType=ISBN13&ItemId=" + isbn
                + "&output=js&Version=20131101&OptResult=ratingInfo,description&Cover=Big";  // description 및 별점 정보 가져오기

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(aladinUrl, String.class);

        // JSON 응답 처리
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);
        JsonNode bookNode = rootNode.path("item").get(0);

        // 책 정보 정리
        ObjectNode bookDetail = objectMapper.createObjectNode();
        bookDetail.put("title", bookNode.path("title").asText());
        bookDetail.put("author", bookNode.path("author").asText());
        bookDetail.put("publisher", bookNode.path("publisher").asText());
        bookDetail.put("price", bookNode.path("priceStandard").asText());
        bookDetail.put("cover", bookNode.path("cover").asText());
        bookDetail.put("pubDate", bookNode.path("pubDate").asText("출판일 정보 없음"));  // pubDate로 출판일 정보 추가
        bookDetail.put("description", bookNode.path("description").asText("설명 없음"));
        bookDetail.put("rating", bookNode.path("subInfo").path("ratingInfo").path("ratingScore").asText("N/A"));

        return bookDetail;
    }

    // 책 검색 기능 (제목, 저자, 출판사 등으로 검색 가능)
    @GetMapping("/search")
    public ObjectNode searchBooks(@RequestParam String text,
                                  @RequestParam(defaultValue = "Title") String category,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  @RequestParam(defaultValue = "Accuracy") String sort) throws Exception {
        // 알라딘 API 쿼리 파라미터 설정 (sort 파라미터 추가)
        String apiUrl = SEARCH_URL + "?ttbkey=" + TTB_KEY
                + "&Query=" + text
                + "&QueryType=" + category
                + "&MaxResults=50&start=1"
                + "&Sort=" + sort // Sort 파라미터 추가
                + "&SearchTarget=Book&output=js&Version=20131101";

        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(apiUrl, String.class);

        // JSON 응답 처리
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(response);

        // 전체 검색 결과 수를 API에서 받아옴
        int totalResults = rootNode.path("totalResults").asInt();  // totalResults 받아옴
        ArrayNode books = (ArrayNode) rootNode.path("item");

        // 페이지네이션 처리 (페이지당 표시할 책 수에 따라 잘라내기)
        int start = (page - 1) * size;
        int end = Math.min(start + size, books.size());

        ArrayNode paginatedBooks = objectMapper.createArrayNode();
        for (int i = start; i < end; i++) {
            JsonNode book = books.get(i);
            String coverUrl = book.get("cover").asText().trim();
            coverUrl = coverUrl.replaceAll("^\"|\"$", "");  // 앞뒤에 있는 큰따옴표 제거

            // ISBN13 값 가져오기
            String isbn13 = book.get("isbn13").asText();

            // 별점 정보 추가 (ISBN을 이용해 별점 조회)
            String lookupUrl = LOOKUP_URL + "?ttbkey=" + TTB_KEY + "&itemIdType=ISBN13&ItemId=" + isbn13
                    + "&output=js&Version=20131101&OptResult=ratingInfo";

            String ratingResponse = restTemplate.getForObject(lookupUrl, String.class);
            JsonNode ratingNode = objectMapper.readTree(ratingResponse);

            String rating = "N/A"; // 기본 값 설정
            if (ratingNode.path("item").size() > 0 && ratingNode.path("item").get(0).path("subInfo").has("ratingInfo")) {
                rating = ratingNode.path("item").get(0).path("subInfo").path("ratingInfo").path("ratingScore").asText("N/A");
            }

            // 정리된 URL과 별점 정보를 다시 JSON 노드에 반영
            ((ObjectNode) book).put("cover", coverUrl);
            ((ObjectNode) book).put("rating", rating); // 별점 추가

            paginatedBooks.add(book);
        }

        // 전체 결과 개수와 페이징된 책 목록을 반환
        ObjectNode result = objectMapper.createObjectNode();
        result.put("totalResults", totalResults); // 총 검색 결과 수
        result.set("books", paginatedBooks); // 책 목록

        return result;
    }
}