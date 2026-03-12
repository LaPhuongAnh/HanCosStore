package com.example.demodatn2.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
// Client gọi Gemini API để sinh câu trả lời chatbot theo prompt hệ thống và người dùng.
public class GeminiClient {

    @Value("${gemini.api.key}")
    private String apiKey;

    // MODEL ĐÚNG HIỆN TẠI
    @Value("${gemini.model:gemini-3-flash-preview}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generate(String systemInstruction, String userMessage) {

        if (apiKey == null || apiKey.isBlank()) {
            return "⚠️ Chatbot chưa được cấu hình API Key";
        }

        String prompt =
                (systemInstruction != null ? systemInstruction + "\n\n" : "")
                        + (userMessage != null ? userMessage : "");

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of("parts", List.of(
                                Map.of("text", prompt)
                        ))
                )
        );

        try {
            Map response = restTemplate.postForObject(url, body, Map.class);

            if (response == null || !response.containsKey("candidates")) {
                return "⚠️ Chatbot không nhận được phản hồi từ AI";
            }

            List candidates = (List) response.get("candidates");
            if (candidates.isEmpty()) {
                return "⚠️ Chatbot chưa có câu trả lời phù hợp";
            }

            Map content = (Map) ((Map) candidates.get(0)).get("content");
            List parts = (List) content.get("parts");

            return (String) ((Map) parts.get(0)).get("text");

        } catch (HttpClientErrorException e) {

            if (e.getStatusCode() == HttpStatus.TOO_MANY_REQUESTS) {
                return "🤖 Chatbot đang quá tải, bạn vui lòng chờ 1–2 phút rồi thử lại nhé!";
            }

            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return "⚠️ Model Gemini không tồn tại hoặc chưa được hỗ trợ";
            }

            return "⚠️ Lỗi gọi Gemini API: " + e.getStatusCode();

        } catch (Exception e) {
            return "⚠️ Không thể kết nối tới chatbot AI";
        }
    }
}
