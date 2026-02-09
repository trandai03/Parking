package com.project.parking.enums;

/**
 * Trạng thái của hóa đơn
 */
public enum InvoiceStatus {
    UNPAID, // Chưa thanh toán
    PAID, // Đã thanh toán
    CANCELLED, // Đã hủy
    OVERDUE // Quá hạn thanh toán
}
