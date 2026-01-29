package com.project.parking.repository;

import com.project.parking.model.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    // Lấy tin nhắn giữa 2 người dùng (theo sender_id và receiver_id)
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.timestamp ASC")
    List<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Lấy tin nhắn giữa 2 người dùng với phân trang
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.timestamp DESC")
    Page<Message> findMessagesBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id, Pageable pageable);

    // Lấy tin nhắn cuối cùng giữa 2 người
    @Query("SELECT m FROM Message m WHERE " +
           "(m.sender.id = :user1Id AND m.receiver.id = :user2Id) OR " +
           "(m.sender.id = :user2Id AND m.receiver.id = :user1Id) " +
           "ORDER BY m.timestamp DESC LIMIT 1")
    Message findLastMessageBetweenUsers(@Param("user1Id") Long user1Id, @Param("user2Id") Long user2Id);

    // Đếm số tin nhắn chưa đọc của một user
    @Query("SELECT COUNT(m) FROM Message m WHERE m.receiver.id = :userId AND m.isRead = false")
    Long countUnreadMessages(@Param("userId") Long userId);
}

