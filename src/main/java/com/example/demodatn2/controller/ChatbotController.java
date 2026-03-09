package com.example.demodatn2.controller;

import com.example.demodatn2.dto.ChatbotRequest;
import com.example.demodatn2.dto.ChatbotResponse;
import com.example.demodatn2.dto.TaiKhoanDTO;
import com.example.demodatn2.service.ChatbotService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping
    public ResponseEntity<ChatbotResponse> chat(@RequestBody(required = false) ChatbotRequest request,
                                                HttpSession session) {
        TaiKhoanDTO loginUser = (TaiKhoanDTO) session.getAttribute("LOGIN_USER");
        String message = request != null ? request.getMessage() : null;
        return ResponseEntity.ok(chatbotService.chat(message, loginUser));
    }
}