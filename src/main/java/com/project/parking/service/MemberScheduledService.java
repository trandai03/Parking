package com.project.parking.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Scheduled service for member-related tasks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MemberScheduledService {

    private final MemberService memberService;

    /**
     * Update expired members status every day at midnight
     * Cron: "0 0 0 * * ?" = At 00:00:00 every day
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
     * Send expiry notification to members 7 days before expiration
     * Cron: "0 0 9 * * ?" = At 09:00:00 every day
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendExpiryNotifications() {
        log.info("Starting scheduled task: Send expiry notifications");
        try {
            // Get members expiring in 7 days
            var expiringMembers = memberService.getMembersExpiringSoon(7);
            log.info("Found {} members expiring in 7 days", expiringMembers.size());
            
            // TODO: Send email notifications to expiring members
            // This can be implemented with EmailService
            
        } catch (Exception e) {
            log.error("Error in scheduled task: Send expiry notifications", e);
        }
    }
}

