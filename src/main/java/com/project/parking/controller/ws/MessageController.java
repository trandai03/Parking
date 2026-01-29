package com.project.parking.controller.ws;

import com.project.parking.dto.MessageDto;
import com.project.parking.model.User;
import com.project.parking.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Objects;

@Controller
@RequiredArgsConstructor
@Slf4j
public class MessageController {
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ChatService chatService;

    // @MessageMapping("/message")
    // @SendTo("/chatroom/public")
    // public MessageDto receiveMessage(@Payload MessageDto messageDto) {
    // return messageDto;
    // }

    @MessageMapping("/private-message")
    public void recMessage(
            SimpMessageHeaderAccessor headerAccessor,
            @Payload MessageDto messageDto) {

        User simpUser = (User) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("simpUser");

        log.info("simpUser: {}", simpUser);
        log.info("Accessor Controller: {}", headerAccessor);

        messageDto.setSenderName(simpUser.getUsername());

        chatService.saveMessageToRedis(messageDto);

        simpMessagingTemplate.convertAndSendToUser(messageDto.getReceiverName(), "/root", messageDto);

    }

}
