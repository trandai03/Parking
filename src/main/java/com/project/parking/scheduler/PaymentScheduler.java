package com.project.parking.scheduler;

import com.project.parking.enums.InvoiceStatus;
import com.project.parking.model.Invoice;
import com.project.parking.model.Member;
import com.project.parking.repository.InvoiceRepository;
import com.project.parking.repository.MemberRepository;
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
    private final InvoiceRepository invoiceRepository;
    private final MemberRepository memberRepository;

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

            List<Invoice> nearDeadlineInvoices = invoiceRepository.findInvoicesNearDeadline(
                    InvoiceStatus.UNPAID, now, deadline);

            int reminderCount = 0;
            for (Invoice invoice : nearDeadlineInvoices) {
                if (invoice.getMemberId() == null)
                    continue;

                Member member = memberRepository.findById(invoice.getMemberId()).orElse(null);
                if (member == null)
                    continue;

                try {
                    paymentService.sendPaymentReminderEmail(member, invoice);
                    reminderCount++;
                } catch (Exception e) {
                    log.error("Failed to send reminder for invoice {}", invoice.getId(), e);
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

            // Tìm các invoice có deadline trong khoảng 2-3 ngày
            List<Invoice> invoices = invoiceRepository.findInvoicesNearDeadline(
                    InvoiceStatus.UNPAID, twoDaysLater, threeDaysLater);

            int reminderCount = 0;
            for (Invoice invoice : invoices) {
                if (invoice.getMemberId() == null)
                    continue;

                Member member = memberRepository.findById(invoice.getMemberId()).orElse(null);
                if (member == null)
                    continue;

                try {
                    paymentService.sendPaymentReminderEmail(member, invoice);
                    reminderCount++;
                } catch (Exception e) {
                    log.error("Failed to send early reminder for invoice {}", invoice.getId(), e);
                }
            }

            log.info("Scheduled task completed: Sent {} early payment reminders", reminderCount);
        } catch (Exception e) {
            log.error("Error in scheduled task: Send early payment reminders", e);
        }
    }

    /**
     * Kiểm tra và đánh dấu các invoice quá hạn thành OVERDUE
     * Chạy mỗi giờ lúc phút 0
     */
    @Scheduled(cron = "0 0 * * * ?")
    public void checkExpiredInvoices() {
        log.info("Starting scheduled task: Check expired invoices");
        try {
            List<Invoice> overdueInvoices = invoiceRepository.findOverdueInvoices(
                    InvoiceStatus.UNPAID, LocalDateTime.now());

            for (Invoice invoice : overdueInvoices) {
                invoice.setStatus(InvoiceStatus.OVERDUE);
                invoiceRepository.save(invoice);
                log.info("Marked invoice {} as OVERDUE", invoice.getId());
            }

            log.info("Scheduled task completed: Checked {} overdue invoices", overdueInvoices.size());
        } catch (Exception e) {
            log.error("Error in scheduled task: Check expired invoices", e);
        }
    }
}
