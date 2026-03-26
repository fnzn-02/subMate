package com.onAir.submate.domain.chat.controller;

import com.onAir.submate.domain.chat.dto.ChatRequest;
import com.onAir.submate.domain.chat.dto.ChatResponse;
import com.onAir.submate.domain.chat.service.ChatService;
import com.onAir.submate.global.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<ChatResponse> chat(@Valid @RequestBody ChatRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        return ResponseEntity.ok(chatService.chat(userId, request));
    }
}
