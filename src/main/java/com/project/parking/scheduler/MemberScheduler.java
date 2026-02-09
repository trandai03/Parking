package com.project.parking.scheduler;

import com.project.parking.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler xử lý các tác vụ tự động liên quan đến member
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MemberScheduler {

    private final MemberService memberService;

    /**
     * Cập nhật trạng thái các member hết hạn
     * Chạy mỗi ngày lúc 00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void updateExpiredMembersDaily() {
        log.info("Starting scheduled task: Update expired members");
        try {
            int updatedCount = memberService.updateExpiredMembers();
            log.info("Scheduled task completed: Updated {} expired members", updatedCount);
        } catch (Exception e) {
            log.error("Error in scheduled task: Update expired members", e);
        }
    }

    /**
     * Gửi thông báo cho các member sắp hết hạn (7 ngày trước)
     * Chạy mỗi ngày lúc 09:00
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendExpiryNotifications() {
        log.info("Starting scheduled task: Send expiry notifications");
        try {
            var expiringMembers = memberService.getMembersExpiringSoon(7);
            log.info("Found {} members expiring in 7 days", expiringMembers.size());

            // TODO: Send email notifications to expiring members
            // This can be implemented with EmailService

        } catch (Exception e) {
            log.error("Error in scheduled task: Send expiry notifications", e);
        }
    }
}
