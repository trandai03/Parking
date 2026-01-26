package com.project.parking.controller;

import com.project.parking.dto.request.*;
import com.project.parking.enums.MemberStatus;
import com.project.parking.response.Response;
import com.project.parking.response.member.MemberResponse;
import com.project.parking.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.v1.prefix:/api/v1}/members")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Member Management", description = "APIs quản lý khách hàng thành viên (Admin only)")
public class MemberController {

    private final MemberService memberService;

    // ============ GET OPERATIONS ============

    @Operation(summary = "Lấy danh sách tất cả members", 
               description = "API này dùng để lấy danh sách tất cả khách hàng thành viên với phân trang")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy danh sách thành công"),
            @ApiResponse(responseCode = "401", description = "Không có quyền truy cập"),
            @ApiResponse(responseCode = "403", description = "Không đủ quyền hạn")
    })
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> getAllMembers(
            @Parameter(description = "Số trang (bắt đầu từ 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Số phần tử mỗi trang") @RequestParam(defaultValue = "10") int size) {
        try {
            log.info("Getting all members - page: {}, size: {}", page, size);
            List<MemberResponse> members = memberService.getAllMembers();
            return ResponseEntity.ok(new Response("success", "Lấy danh sách member thành công", members));
        } catch (Exception e) {
            log.error("Error getting all members", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy thông tin member theo ID", 
               description = "API này dùng để lấy thông tin chi tiết của một thành viên theo ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> getMemberById(
            @Parameter(description = "ID của member") @PathVariable Long id) {
        try {
            log.info("Getting member by id: {}", id);
            MemberResponse member = memberService.getMemberById(id);
            return ResponseEntity.ok(new Response("success", "Lấy thông tin member thành công", member));
        } catch (Exception e) {
            log.error("Error getting member by id: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy thông tin member theo mã thẻ", 
               description = "API này dùng để lấy thông tin thành viên theo mã thẻ member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thông tin thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @GetMapping("/code/{memberCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> getMemberByCode(
            @Parameter(description = "Mã thẻ member") @PathVariable String memberCode) {
        try {
            log.info("Getting member by code: {}", memberCode);
            MemberResponse member = memberService.getMemberByCode(memberCode);
            return ResponseEntity.ok(new Response("success", "Lấy thông tin member thành công", member));
        } catch (Exception e) {
            log.error("Error getting member by code: {}", memberCode, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ SEARCH OPERATIONS ============

    @Operation(summary = "Tìm kiếm members", 
               description = "API tìm kiếm member theo nhiều tiêu chí: số điện thoại, biển số xe, mã thẻ, tên, email, loại gói, trạng thái")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tìm kiếm thành công")
    })
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> searchMembers(@RequestBody MemberSearchRequest request) {
        try {
            log.info("Searching members with criteria: {}", request);
            List<MemberResponse> members = memberService.searchMembers(request);
            return ResponseEntity.ok(new Response("success", "Tìm kiếm thành công", members));
        } catch (Exception e) {
            log.error("Error searching members", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tìm member theo số điện thoại", 
               description = "API tìm kiếm nhanh theo số điện thoại")
    @GetMapping("/search/phone/{phoneNumber}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> searchByPhone(
            @Parameter(description = "Số điện thoại") @PathVariable String phoneNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        try {
            MemberSearchRequest request = MemberSearchRequest.builder()
                    .phoneNumber(phoneNumber)
                    .page(page)
                    .size(size)
                    .build();
            Page<MemberResponse> members = (Page<MemberResponse>) memberService.searchMembers(request);
            return ResponseEntity.ok(new Response("success", "Tìm kiếm thành công", members));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tìm member theo biển số xe", 
               description = "API tìm kiếm member thông qua biển số xe đã đăng ký")
    @GetMapping("/search/license-plate/{licensePlate}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> searchByLicensePlate(
            @Parameter(description = "Biển số xe") @PathVariable String licensePlate) {
        try {
            MemberSearchRequest request = MemberSearchRequest.builder()
                    .licensePlate(licensePlate)
                    .build();
            List<MemberResponse> members = memberService.searchMembers(request);
            return ResponseEntity.ok(new Response("success", "Tìm kiếm thành công", members));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ USER REGISTER MEMBER ============

    @Operation(summary = "User đăng ký làm member", 
               description = "API cho user đăng ký làm thành viên (trạng thái PENDING, chờ Owner duyệt)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Đăng ký thành công, chờ duyệt"),
            @ApiResponse(responseCode = "400", description = "User đã đăng ký member rồi")
    })
    @PostMapping("/register/{userId}")
    public ResponseEntity<Response> registerMember(
            @Parameter(description = "ID của user") @PathVariable Long userId,
            @RequestBody @Valid CreateMemberRequest request) {
        try {
            log.info("User {} registering as member", userId);
            MemberResponse member = memberService.registerMember(userId, request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new Response("success", "Đăng ký thành công, vui lòng chờ duyệt", member));
        } catch (Exception e) {
            log.error("Error registering member for user: {}", userId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Kiểm tra user đã là member chưa")
    @GetMapping("/check/{userId}")
    public ResponseEntity<Response> checkMembership(@PathVariable Long userId) {
        try {
            boolean hasMembership = memberService.hasMembership(userId);
            return ResponseEntity.ok(new Response("success", "Kiểm tra thành công", hasMembership));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy thông tin member của user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<Response> getMemberByUserId(@PathVariable Long userId) {
        try {
            MemberResponse member = memberService.getMemberByUserId(userId);
            return ResponseEntity.ok(new Response("success", "Lấy thông tin thành công", member));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ APPROVAL OPERATIONS (OWNER) ============

    @Operation(summary = "Lấy danh sách member chờ duyệt", 
               description = "API cho Owner xem danh sách đăng ký member đang chờ duyệt")
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getPendingMembers() {
        try {
            log.info("Getting pending members");
            List<MemberResponse> members = memberService.getPendingMembers();
            return ResponseEntity.ok(new Response("success", "Lấy danh sách thành công", members));
        } catch (Exception e) {
            log.error("Error getting pending members", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy danh sách member chờ duyệt theo bãi đỗ xe")
    @GetMapping("/pending/parking-lot/{parkingLotId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getPendingMembersByParkingLot(@PathVariable Long parkingLotId) {
        try {
            log.info("Getting pending members for parking lot: {}", parkingLotId);
            List<MemberResponse> members = memberService.getPendingMembersByParkingLot(parkingLotId);
            return ResponseEntity.ok(new Response("success", "Lấy danh sách thành công", members));
        } catch (Exception e) {
            log.error("Error getting pending members for parking lot: {}", parkingLotId, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Duyệt đăng ký member", 
               description = "API cho Owner duyệt đơn đăng ký member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Duyệt thành công"),
            @ApiResponse(responseCode = "400", description = "Member không ở trạng thái chờ duyệt")
    })
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> approveMember(@PathVariable Long id) {
        try {
            log.info("Approving member: {}", id);
            MemberResponse member = memberService.approveMember(id);
            return ResponseEntity.ok(new Response("success", "Duyệt thành công", member));
        } catch (Exception e) {
            log.error("Error approving member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Từ chối đăng ký member", 
               description = "API cho Owner từ chối đơn đăng ký member")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Từ chối thành công"),
            @ApiResponse(responseCode = "400", description = "Member không ở trạng thái chờ duyệt")
    })
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> rejectMember(
            @PathVariable Long id,
            @Parameter(description = "Lý do từ chối") @RequestParam(required = false) String reason) {
        try {
            log.info("Rejecting member: {}", id);
            MemberResponse member = memberService.rejectMember(id, reason);
            return ResponseEntity.ok(new Response("success", "Từ chối thành công", member));
        } catch (Exception e) {
            log.error("Error rejecting member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ UPDATE OPERATIONS ============

    @Operation(summary = "Cập nhật thông tin member", 
               description = "API này dùng để cập nhật thông tin cá nhân của thành viên")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> updateMember(
            @Parameter(description = "ID của member") @PathVariable Long id,
            @RequestBody @Valid UpdateMemberRequest request) {
        try {
            log.info("Updating member: {}", id);
            MemberResponse member = memberService.updateMember(id, request);
            return ResponseEntity.ok(new Response("success", "Cập nhật thông tin thành công", member));
        } catch (Exception e) {
            log.error("Error updating member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ LOCK/UNLOCK OPERATIONS ============

    @Operation(summary = "Khóa thẻ member", 
               description = "API này dùng để khóa thẻ thành viên")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Khóa thẻ thành công"),
            @ApiResponse(responseCode = "400", description = "Thẻ đã bị khóa trước đó"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> lockMember(
            @Parameter(description = "ID của member") @PathVariable Long id,
            @RequestBody @Valid LockMemberRequest request) {
        try {
            log.info("Locking member: {}", id);
            MemberResponse member = memberService.lockMember(id, request);
            return ResponseEntity.ok(new Response("success", "Khóa thẻ thành công", member));
        } catch (Exception e) {
            log.error("Error locking member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Mở khóa thẻ member", 
               description = "API này dùng để mở khóa thẻ thành viên đã bị khóa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Mở khóa thành công"),
            @ApiResponse(responseCode = "400", description = "Thẻ không ở trạng thái bị khóa"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> unlockMember(
            @Parameter(description = "ID của member") @PathVariable Long id) {
        try {
            log.info("Unlocking member: {}", id);
            MemberResponse member = memberService.unlockMember(id);
            return ResponseEntity.ok(new Response("success", "Mở khóa thẻ thành công", member));
        } catch (Exception e) {
            log.error("Error unlocking member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Hủy thẻ member", 
               description = "API này dùng để hủy thẻ thành viên (không thể khôi phục)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Hủy thẻ thành công"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> cancelMember(
            @Parameter(description = "ID của member") @PathVariable Long id,
            @Parameter(description = "Lý do hủy thẻ") @RequestParam(required = false) String reason) {
        try {
            log.info("Cancelling member: {}", id);
            MemberResponse member = memberService.cancelMember(id, reason);
            return ResponseEntity.ok(new Response("success", "Hủy thẻ thành công", member));
        } catch (Exception e) {
            log.error("Error cancelling member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ RENEW OPERATIONS ============

    @Operation(summary = "Gia hạn thẻ member", 
               description = "API này dùng để gia hạn thẻ thành viên theo gói tháng/quý/năm")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Gia hạn thành công"),
            @ApiResponse(responseCode = "400", description = "Không thể gia hạn thẻ đã bị hủy"),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy member")
    })
    @PostMapping("/{id}/renew")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> renewMember(
            @Parameter(description = "ID của member") @PathVariable Long id,
            @RequestBody @Valid RenewMemberRequest request) {
        try {
            log.info("Renewing member: {} with plan: {}", id, request.getPlanId());
            MemberResponse member = memberService.renewMember(id, request);
            return ResponseEntity.ok(new Response("success", "Gia hạn thẻ thành công", member));
        } catch (Exception e) {
            log.error("Error renewing member: {}", id, e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ STATISTICS OPERATIONS ============

    @Operation(summary = "Lấy thống kê members", 
               description = "API này dùng để lấy thống kê tổng quan về thành viên")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lấy thống kê thành công")
    })
    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
    public ResponseEntity<Response> getMemberStatistics() {
        try {
            log.info("Getting member statistics");
            Map<String, Object> stats = memberService.getMemberStatistics();
            return ResponseEntity.ok(new Response("success", "Lấy thống kê thành công", stats));
        } catch (Exception e) {
            log.error("Error getting member statistics", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Lấy danh sách members sắp hết hạn", 
               description = "API này dùng để lấy danh sách thành viên sắp hết hạn trong X ngày tới")
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'EMPLOYEE')")
    public ResponseEntity<Response> getMembersExpiringSoon(
            @Parameter(description = "Số ngày tới") @RequestParam(defaultValue = "7") int days) {
        try {
            log.info("Getting members expiring in {} days", days);
            List<MemberResponse> members = memberService.getMembersExpiringSoon(days);
            return ResponseEntity.ok(new Response("success", "Lấy danh sách thành công", members));
        } catch (Exception e) {
            log.error("Error getting expiring members", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    // ============ PRICING OPERATIONS ============

    @Operation(summary = "Lấy bảng giá gói thành viên", 
               description = "API này dùng để lấy bảng giá các gói thành viên (tháng/quý/năm)")
    @GetMapping("/pricing")
    public ResponseEntity<Response> getMembershipPricing() {
        try {
            log.info("Getting membership pricing");
            Map<String, BigDecimal> fees = memberService.getMembershipFees();
            return ResponseEntity.ok(new Response("success", "Lấy bảng giá thành công", fees));
        } catch (Exception e) {
            log.error("Error getting membership pricing", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }

    @Operation(summary = "Tính phí cho gói đăng ký", 
               description = "API này dùng để tính phí cho một gói đăng ký cụ thể")
    @GetMapping("/pricing/plan/{planId}")
    public ResponseEntity<Response> calculateFee(
            @Parameter(description = "ID của gói đăng ký") @PathVariable Long planId) {
        try {
            log.info("Calculating fee for plan: {}", planId);
            BigDecimal fee = memberService.calculatePlanFee(planId);
            return ResponseEntity.ok(new Response("success", "Tính phí thành công", fee));
        } catch (Exception e) {
            log.error("Error calculating fee", e);
            return ResponseEntity.badRequest().body(new Response("error", e.getMessage(), null));
        }
    }
}

