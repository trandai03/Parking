package com.project.parking.service;
import com.project.parking.dto.MessageDto;
import com.project.parking.model.Message;
import com.project.parking.model.User;
import com.project.parking.repository.MessageRepository;
import com.project.parking.repository.ThreadRepository;
import com.project.parking.repository.UserRepository;
import com.project.parking.response.chat.CategoryChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService {
    private final RedisTemplate<String, MessageDto> redisTemplate;
    private final UserService userService;
    private final ThreadRepository threadRepository;
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;


    @Transactional
    public void saveMessageToRedis(MessageDto message) {
        // 1. Lưu vào Database trước
        saveMessageToDatabase(message);
        
        // 2. Sau đó lưu vào Redis để cache
        String key = String.format("messages:%s-%s", message.getSenderName(), message.getReceiverName());

        Map<String, String> avatarAndName = userService.getAvatarAndNameByUsernames(message.getReceiverName());

        log.info("Avatar and name: {}", avatarAndName);

        message.setAvatar(avatarAndName.get("avatar"));
        message.setFullNameReceiver(avatarAndName.get("name"));

        redisTemplate.opsForList().rightPush(key, message);
        redisTemplate.expire(key, 1, java.util.concurrent.TimeUnit.DAYS);
    }
    
    @Transactional
    public void saveMessageToDatabase(MessageDto messageDto) {
        try {
            // Tìm sender và receiver
            User sender = userRepository.findByUsername(messageDto.getSenderName())
                .orElseThrow(() -> new RuntimeException("Sender not found: " + messageDto.getSenderName()));
            User receiver = userRepository.findByUsername(messageDto.getReceiverName())
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + messageDto.getReceiverName()));

            // Tạo Message entity
            Message message = new Message();
            message.setSender(sender);
            message.setReceiver(receiver);
            message.setContent(messageDto.getMessage());
            message.setIsRead(false);
            // timestamp sẽ được tự động set bởi @CreatedDate

            // Lưu vào database
            messageRepository.save(message);
            
            log.info("✅ Message saved to database: {} -> {}", messageDto.getSenderName(), messageDto.getReceiverName());
            
        } catch (Exception e) {
            log.error("❌ Error saving message to database: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save message to database", e);
        }
    }


    public List<MessageDto> getMessage(String senderName, String receiverName) {
        try {
            // 1. Tìm user IDs
            User sender = userRepository.findByUsername(senderName)
                .orElseThrow(() -> new RuntimeException("Sender not found: " + senderName));
            User receiver = userRepository.findByUsername(receiverName)
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + receiverName));

            // 2. Lấy tin nhắn từ Database (chính xác và đầy đủ)
            List<Message> messages = messageRepository.findMessagesBetweenUsers(sender.getId(), receiver.getId());
            
            // 3. Convert sang DTO
            List<MessageDto> messageDtos = messages.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

            log.info("✅ Loaded {} messages from database between {} and {}", 
                messageDtos.size(), senderName, receiverName);
            
            return messageDtos;
            
        } catch (Exception e) {
            log.error("❌ Error loading messages from database: {}", e.getMessage(), e);
            
            // Fallback to Redis if database fails
            return getMessageFromRedis(senderName, receiverName);
        }
    }
    
    private List<MessageDto> getMessageFromRedis(String senderName, String receiverName) {
        // Khóa theo cả hai chiều
        String key1 = String.format("messages:%s-%s", senderName, receiverName);
        String key2 = String.format("messages:%s-%s", receiverName, senderName);

        // Tập hợp tin nhắn
        List<MessageDto> allMessages = new ArrayList<>();

        // Lấy tin nhắn từ khóa thứ nhất
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key1))) {
            List<MessageDto> messagesFromKey1 = redisTemplate.opsForList().range(key1, 0, -1);
            if (messagesFromKey1 != null) {
                allMessages.addAll(messagesFromKey1);
            }
        }

        // Lấy tin nhắn từ khóa thứ hai
        if (Boolean.TRUE.equals(redisTemplate.hasKey(key2))) {
            List<MessageDto> messagesFromKey2 = redisTemplate.opsForList().range(key2, 0, -1);
            if (messagesFromKey2 != null) {
                allMessages.addAll(messagesFromKey2);
            }
        }

        // Sắp xếp tin nhắn theo thời gian
        allMessages.sort(Comparator.comparing(MessageDto::getTimestamp));

        return allMessages;
    }
    
    private MessageDto convertToDto(Message message) {
        Map<String, String> receiverInfo = userService.getAvatarAndNameByUsernames(message.getReceiver().getUsername());
        
        return MessageDto.builder()
            .senderName(message.getSender().getUsername())
            .receiverName(message.getReceiver().getUsername())
            .message(message.getContent())
            .fullNameReceiver(receiverInfo.get("name"))
            .avatar(receiverInfo.get("avatar"))
            .timestamp(message.getTimestamp() != null ? 
                LocalDateTime.ofInstant(message.getTimestamp(), java.time.ZoneId.systemDefault()) : 
                LocalDateTime.now())
            .build();
    }

    //
    public List<CategoryChatResponse> getUserChatCategories(String userId) {
        // Danh sách các cuộc trò chuyện của người dùng
        List<CategoryChatResponse> categoryChatList = new ArrayList<>();

        // Lấy tất cả các khóa liên quan đến tin nhắn
        Set<String> keys = redisTemplate.keys("messages:*");

        // Tập hợp để lưu trữ các cuộc trò chuyện đã xử lý
        Set<String> processedConversations = new HashSet<>();

        // Duyệt qua các khóa
        for (String key : keys) {
            String[] keyParts = key.split("[:-]");
            String sender = keyParts[1];
            String receiver = keyParts[2];

            // Kiểm tra nếu userId liên quan đến khóa này
            if (!sender.equals(userId) && !receiver.equals(userId)) continue;

            // Tạo một khóa chuẩn hóa để kiểm tra trùng lặp
            String normalizedKey = sender.compareTo(receiver) < 0
                    ? sender + "-" + receiver
                    : receiver + "-" + sender;

            // Nếu cuộc trò chuyện đã được xử lý, bỏ qua
            if (processedConversations.contains(normalizedKey)) continue;
            processedConversations.add(normalizedKey);

            // Lấy tin nhắn cuối cùng từ Redis
            List<MessageDto> messages = redisTemplate.opsForList().range(key, 0, -1);
            if (messages != null && !messages.isEmpty()) {
                MessageDto lastMessage = messages.get(messages.size() - 1);

                // Xác định "người kia"
                String otherUser = sender.equals(userId) ? receiver : sender;

                Map<String, String> nameAndAvatar = userService.getAvatarAndNameByUsernames(otherUser);

                // Tạo đối tượng phản hồi
                CategoryChatResponse categoryChat = new CategoryChatResponse();
                categoryChat.setUsername(otherUser);
                categoryChat.setFullName(nameAndAvatar.get("name"));
                categoryChat.setAvatar(nameAndAvatar.get("avatar"));
                categoryChat.setLastMessage(lastMessage.getMessage());
                categoryChat.setLastMessageTime(String.valueOf(lastMessage.getTimestamp()));

                categoryChatList.add(categoryChat);
            }
        }


        return categoryChatList;
    }

    /**
     * Đánh dấu tất cả tin nhắn giữa 2 user là đã đọc
     */
    @Transactional
    public void markMessagesAsRead(String currentUsername, String otherUsername) {
        try {
            User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + currentUsername));
            User otherUser = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new RuntimeException("User not found: " + otherUsername));

            // Tìm tất cả tin nhắn từ otherUser gửi cho currentUser và đánh dấu đã đọc
            List<Message> unreadMessages = messageRepository.findMessagesBetweenUsers(currentUser.getId(), otherUser.getId())
                .stream()
                .filter(msg -> msg.getReceiver().getId().equals(currentUser.getId()) && !msg.getIsRead())
                .collect(Collectors.toList());

            unreadMessages.forEach(msg -> msg.setIsRead(true));
            messageRepository.saveAll(unreadMessages);

            log.info("✅ Marked {} messages as read between {} and {}", 
                unreadMessages.size(), currentUsername, otherUsername);
                
        } catch (Exception e) {
            log.error("❌ Error marking messages as read: {}", e.getMessage(), e);
        }
    }

    /**
     * Đếm số tin nhắn chưa đọc của user
     */
    public long getUnreadMessageCount(String username) {
        try {
            User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            return messageRepository.countUnreadMessages(user.getId());
        } catch (Exception e) {
            log.error("❌ Error counting unread messages: {}", e.getMessage(), e);
            return 0;
        }
    }

}
