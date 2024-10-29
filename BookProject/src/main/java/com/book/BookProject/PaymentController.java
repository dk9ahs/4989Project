package com.book.BookProject;

import com.book.BookProject.order.PaymentInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Value("${iamport.api_key}")
    private String apiKey;

    @Value("${iamport.api_secret}")
    private String apiSecret;

    // 결제 검증
    @PostMapping("/verifyPayment")
    public ResponseEntity<Map<String, Object>> verifyPayment(@RequestBody Map<String, String> request) {
        String impUid = request.get("imp_uid");
        Map<String, Object> response = new HashMap<>();

        try {
            String accessToken = getIamportAccessToken();
            PaymentInfo paymentInfo = getPaymentData(impUid, accessToken);

            ObjectMapper objectMapper = new ObjectMapper();
            String jsonPaymentInfo = objectMapper.writeValueAsString(paymentInfo);
            System.out.println("Serialized PaymentInfo: " + jsonPaymentInfo);  // 직렬화된 JSON 확인

            if (paymentInfo != null && paymentInfo.getAmount() > 0) {
                response.put("success", true);
                response.put("paymentInfo", paymentInfo);  // 직렬화된 paymentInfo 반환
            } else {
                response.put("success", false);
                response.put("message", "결제 검증 실패");
            }

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "결제 검증 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 환불 처리
    @PostMapping("/refundPayment")
    public ResponseEntity<Map<String, Object>> refundPayment(@RequestBody Map<String, String> request) {
        String impUid = request.get("imp_uid");
        Integer amount = Integer.parseInt(request.get("amount"));
        Map<String, Object> response = new HashMap<>();

        try {
            // 액세스 토큰 발급
            String accessToken = getIamportAccessToken();

            // 환불 요청
            String url = "https://api.iamport.kr/payments/cancel";

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("imp_uid", impUid);
            requestBody.put("amount", amount);

            Map<String, Object> responseBody = sendRequest(url, HttpMethod.POST, requestBody, accessToken);

            // 환불 결과 확인
            if (responseBody != null && responseBody.get("code") != null && ((Integer) responseBody.get("code")) == 0) {
                response.put("success", true);
                response.put("message", "환불이 성공적으로 처리되었습니다.");
            } else {
                response.put("success", false);
                response.put("message", "환불 처리에 실패했습니다. 응답: " + responseBody);
            }
            return ResponseEntity.ok(response);
        } catch (RestClientException e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "통신 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "환불 처리 중 오류 발생: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // 액세스 토큰 발급
    private String getIamportAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";

        // 요청 본문 데이터 준비
        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("imp_key", apiKey);
        requestBody.put("imp_secret", apiSecret);

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);  // JSON 형식 설정

            // ObjectMapper를 사용하여 JSON으로 변환
            ObjectMapper objectMapper = new ObjectMapper();
            String jsonBody = objectMapper.writeValueAsString(requestBody);

            // 요청 엔터티 생성
            HttpEntity<String> entity = new HttpEntity<>(jsonBody, headers);

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, entity, Map.class);

            // 응답 처리
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && ((Integer) responseBody.get("code")) == 0) {
                Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
                return (String) responseData.get("access_token");
            } else {
                throw new RuntimeException("Failed to get Iamport access token: " + responseBody);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception occurred while requesting Iamport access token: " + e.getMessage());
        }
    }

    // 결제 정보 조회
    private PaymentInfo getPaymentData(String impUid, String accessToken) {
        String url = "https://api.iamport.kr/payments/" + impUid;

        try {
            Map<String, Object> responseBody = sendRequest(url, HttpMethod.GET, null, accessToken);

            if (responseBody != null && ((Integer) responseBody.get("code")) == 0) {
                Map<String, Object> responseData = (Map<String, Object>) responseBody.get("response");
                return new PaymentInfo(
                        (String) responseData.get("imp_uid"),
                        (String) responseData.get("merchant_uid"),
                        ((Number) responseData.get("amount")).intValue()
                );
            }
            throw new RuntimeException("Failed to get payment data from Iamport");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Exception occurred while getting payment data: " + e.getMessage());
        }
    }

    // 공통 요청 메서드
    private Map<String, Object> sendRequest(String url, HttpMethod method, Map<String, Object> requestBody, String accessToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        if (accessToken != null) {
            headers.set("Authorization", "Bearer " + accessToken);
        }

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, method, entity, Map.class);
        return response.getBody();
    }

}