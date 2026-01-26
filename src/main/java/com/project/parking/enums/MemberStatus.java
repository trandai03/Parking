package com.project.parking.enums;

/**
 * Enum representing the status of a member's account.
 */
public enum MemberStatus {
    PENDING,    // Chờ duyệt
    WAITING_PAYMENT, // Chờ thanh toán
    ACTIVE,     // Thẻ đang hoạt động (đã được duyệt)
    REJECTED,   // Bị từ chối
    LOCKED,     // Thẻ bị khóa
    EXPIRED,    // Thẻ hết hạn
    CANCELLED   // Thẻ bị hủy
}

