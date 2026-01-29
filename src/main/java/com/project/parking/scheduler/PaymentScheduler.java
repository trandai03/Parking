package com.project.parking.scheduler;

import com.project.parking.enums.PaymentStatus;
import com.project.parking.model.PaymentHistory;
import com.project.parking.repository.PaymentHistoryRepository;
import com.project.parking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Scheduler để tự động xử lý các tác vụ liên quan đến thanh toán
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentScheduler {

    private final PaymentService paymentService;
    private final PaymentHistoryRepository paymentHistoryRepository;

    /**
     * Kiểm tra và khóa các member không thanh toán quá hạn
     * Chạy mỗi ngày lúc 00:00
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void checkOverduePayments() {
        log.info("Starting scheduled task: Check overdue payments");
        try {
            int lockedCount = paymentService.lockOverdueMembers();
            log.info("Scheduled task completed: Locked {} members due to overdue payment", lockedCount);
        } catch (Exception e) {
            log.error("Error in scheduled task: Check overdue payments", e);
        }
    }

    /**
     * Gửi email nhắc nhở cho các member sắp hết hạn thanh toán (còn 1 ngày)
     * Chạy mỗi ngày lúc 09:00
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendPaymentReminders() {
        log.info("Starting scheduled task: Send payment reminders");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime deadline = now.plusDays(1);

            List<PaymentHistory> nearDeadlinePayments = paymentHistoryRepository.findPaymentsNearDeadline(
                    PaymentStatus.PENDING, now, deadline);

            int reminderCount = 0;
            for (PaymentHistory payment : nearDeadlinePayments) {
                try {
                    paymentService.sendPaymentReminderEmail(payment.getMember(), payment);
                    reminderCount++;
                } catch (Exception e) {
                    log.error("Failed to send reminder for payment {}", payment.getId(), e);
                }
            }

            log.info("Scheduled task completed: Sent {} payment reminders", reminderCount);
        } catch (Exception e) {
            log.error("Error in scheduled task: Send payment reminders", e);
        }
    }

    /**
     * Gửi email nhắc nhở cho các member sắp hết hạn thanh toán (còn 3 ngày)
     * Chạy mỗi ngày lúc 10:00
     */
    @Scheduled(cron = "0 0 10 * * ?")
    public void sendEarlyPaymentReminders() {
        log.info("Starting scheduled task: Send early payment reminders (3 days before deadline)");
        try {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime threeDaysLater = now.plusDays(3);
            LocalDateTime twoDaysLater = now.plusDays(2);

            // Tìm các payment có deadline trong khoảng 2-3 ngày
            List<PaymentHistory> payments = paymentHistoryRepository.findPaymentsNearDeadline(
                    PaymentStatus.PENDING, twoDaysLater, threeDaysLater);

            int reminderCount = 0;
            for (PaymentHistory payment : payments) {
                try {
                    paymentService.sendPaymentReminderEmail(payment.getMember(), payment);
                    reminderCount++;
                } catch (Exception e) {
                    log.error("Failed to send early reminder for payment {}", payment.getId(), e);
                }
            }

            log.info("Scheduled task completed: Sent {} early payment reminders", reminderCount);
        } catch (Exception e) {
            log.error("Error in scheduled task: Send early payment reminders", e);
        }
    }
}

