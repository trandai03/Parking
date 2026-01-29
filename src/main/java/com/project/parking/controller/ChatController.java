package com.project.parking.controller;
import com.project.parking.dto.MessageDto;
import com.project.parking.model.User;
import com.project.parking.response.Response;
import com.project.parking.response.ResponseList;
import com.project.parking.response.chat.CategoryChatResponse;
import com.project.parking.service.ChatService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("${api.v1.prefix:/api/v1}/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping
    public ResponseEntity<ResponseList<List<MessageDto>>> getMessages(
            @AuthenticationPrincipal User user,
            @RequestParam String receiverName,
            @RequestParam(defaultValue = "0", required = false) int page,
            @RequestParam(defaultValue = "10", required = false) int size
    ) {
        List<MessageDto> messages = chatService.getMessage(user.getUsername(), receiverName);
        
        // Đánh dấu tin nhắn là đã đọc khi load
        chatService.markMessagesAsRead(user.getUsername(), receiverName);

        return ResponseEntity.ok(ResponseList.<List<MessageDto>>builder()
                .data(messages)
                .message("Messages retrieved successfully")
                .build());
    }

    @GetMapping("/categories")
    public ResponseEntity<ResponseList<List<CategoryChatResponse>>> getChatCategories(
            @AuthenticationPrincipal User user
    ) {
        List<CategoryChatResponse> chatCategories = chatService.getUserChatCategories(user.getUsername());

        return ResponseEntity.ok(ResponseList.<List<CategoryChatResponse>>builder()
                .data(chatCategories)
                .message("Chat categories retrieved successfully")
                .build());
    }
    
    @GetMapping("/unread-count")
    public ResponseEntity<Response> getUnreadMessageCount(
            @AuthenticationPrincipal User user
    ) {
        long unreadCount = chatService.getUnreadMessageCount(user.getUsername());
        
        return ResponseEntity.ok(Response.builder()
                .data(unreadCount)
                .message("Unread message count retrieved successfully")
                .build());
    }
}
