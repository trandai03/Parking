package com.project.parking.response.member;

import com.project.parking.enums.MemberStatus;
import com.project.parking.model.Member;
import com.project.parking.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Getter
@Setter
@Builder
@Slf4j
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Response object for member information")
public class MemberResponse {

    @Schema(description = "ID của member")
    private Long id;

    @Schema(description = "ID của user")
    private Long userId;

    @Schema(description = "Mã thẻ member", example = "MEM-2026-00001")
    private String memberCode;

    // User info
    @Schema(description = "Tên đăng nhập")
    private String username;

    @Schema(description = "Email")
    private String email;

    @Schema(description = "Họ và tên")
    private String fullname;

    @Schema(description = "Số điện thoại")
    private String phoneNumber;

    @Schema(description = "Ngày sinh")
    private LocalDate dateOfBirth;

    // Parking lot info
    @Schema(description = "ID bãi đỗ xe")
    private Long parkingLotId;

    @Schema(description = "Tên bãi đỗ xe")
    private String parkingLotName;

    // Plan info
    @Schema(description = "ID gói đăng ký")
    private Long planId;

    @Schema(description = "Tên gói đăng ký")
    private String planName;

    // Membership info

    @Schema(description = "Trạng thái thẻ member")
    private MemberStatus memberStatus;

    @Schema(description = "Ngày bắt đầu thẻ")
    private LocalDateTime membershipStartDate;

    @Schema(description = "Ngày hết hạn thẻ")
    private LocalDateTime membershipExpiryDate;

    @Schema(description = "Phí thành viên")
    private BigDecimal membershipFee;

    @Schema(description = "Ngày khóa (nếu có)")
    private LocalDateTime lockedAt;

    @Schema(description = "Lý do khóa (nếu có)")
    private String lockReason;

    @Schema(description = "Ngày tạo")
    private LocalDateTime createdAt;

    @Schema(description = "Ngày cập nhật")
    private LocalDateTime updatedAt;

    @Schema(description = "Số ngày còn lại")
    private Long daysRemaining;

    @Schema(description = "Thẻ còn hiệu lực")
    private Boolean isValid;

    public static MemberResponse fromMember(Member member) {
        if (member == null) {
            log.error("MemberResponse fromMember: member is null");
            return null;
        }

        User user = member.getUser();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = member.getMembershipExpiryDate();
        
        Long daysRemaining = null;
        Boolean isValid = false;
        
        if (expiryDate != null) {
            daysRemaining = java.time.Duration.between(now, expiryDate).toDays();
            if (daysRemaining < 0) daysRemaining = 0L;
            isValid = expiryDate.isAfter(now) && 
                      member.getMemberStatus() == MemberStatus.ACTIVE;
        }

        MemberResponseBuilder builder = MemberResponse.builder()
                .id(member.getId())
                .memberCode(member.getMemberCode())
                .memberStatus(member.getMemberStatus())
                .membershipStartDate(member.getMembershipStartDate())
                .membershipExpiryDate(member.getMembershipExpiryDate())
                .membershipFee(member.getMembershipFee())
                .lockedAt(member.getLockedAt())
                .lockReason(member.getLockReason())
                .createdAt(member.getCreatedAt())
                .updatedAt(member.getUpdatedAt())
                .daysRemaining(daysRemaining)
                .isValid(isValid);

        // User info
        if (user != null) {
            builder.userId(user.getId())
                   .username(user.getUsername())
                   .email(user.getEmail())
                   .fullname(user.getFullname())
                   .phoneNumber(user.getPhoneNumber())
                   .dateOfBirth(user.getDateOfBirth());
        }

        // Parking lot info
        if (member.getParkingLot() != null) {
            builder.parkingLotId(member.getParkingLot().getId())
                   .parkingLotName(member.getParkingLot().getName());
        }

        // Plan info
        if (member.getParkingPlan() != null) {
            builder.planId(member.getParkingPlan().getId())
                   .planName(member.getParkingPlan().getName());
        }

        return builder.build();
    }

    public static List<MemberResponse> fromMembers(List<Member> members) {
        return members.stream()
                .map(MemberResponse::fromMember)
                .collect(Collectors.toList());
    }
}
