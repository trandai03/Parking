package com.project.parking.repository;

import com.project.parking.enums.MemberStatus;
import com.project.parking.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    /**
     * Find member by member code
     */
    Optional<Member> findByMemberCode(String memberCode);

    /**
     * Check if member code exists
     */
    boolean existsByMemberCode(String memberCode);

    /**
     * Find member by user id
     */
    Optional<Member> findByUserId(Long userId);

    /**
     * Check if user already has membership
     */
    boolean existsByUserId(Long userId);

    /**
     * Find all members by parking lot
     */
    List<Member> findByParkingLotId(Long parkingLotId);

    /**
     * Find members by status
     */
    List<Member> findByMemberStatus(MemberStatus memberStatus);

    /**
     * Find members by parking plan
     */
    List<Member> findByParkingPlanId(Long parkingPlanId);

    /**
     * Find members by parking lot and status
     */
    List<Member> findByParkingLotIdAndMemberStatus(Long parkingLotId, MemberStatus memberStatus);

    /**
     * Find members with expiring membership (within X days)
     */
    @Query("SELECT m FROM Member m WHERE m.memberStatus = 'ACTIVE' " +
           "AND m.membershipExpiryDate BETWEEN :now AND :expiryDate")
    List<Member> findMembersExpiringBefore(@Param("now") LocalDateTime now, 
                                           @Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find expired members
     */
    @Query("SELECT m FROM Member m WHERE m.membershipExpiryDate < :now " +
           "AND m.memberStatus = 'ACTIVE'")
    List<Member> findExpiredMembers(@Param("now") LocalDateTime now);

    /**
     * Search members by phone number through user
     */
    @Query("SELECT m FROM Member m JOIN m.user u WHERE u.phoneNumber LIKE %:phoneNumber%")
    List<Member> findByUserPhoneNumber(@Param("phoneNumber") String phoneNumber);

    /**
     * Search members by user email
     */
    @Query("SELECT m FROM Member m JOIN m.user u WHERE LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<Member> findByUserEmail(@Param("email") String email);

    /**
     * Search members by keyword (fullname, username)
     */
    @Query("SELECT m FROM Member m JOIN m.user u WHERE " +
           "LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Member> findByKeyword(@Param("keyword") String keyword);

    /**
     * Advanced search for members
     */
    @Query("SELECT m FROM Member m JOIN m.user u WHERE " +
           "(:parkingLotId IS NULL OR m.parkingLot.id = :parkingLotId) " +
           "AND (:phoneNumber IS NULL OR u.phoneNumber LIKE %:phoneNumber%) " +
           "AND (:memberCode IS NULL OR m.memberCode LIKE %:memberCode%) " +
           "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:keyword IS NULL OR LOWER(u.fullname) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "     OR LOWER(u.username) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "AND (:memberStatus IS NULL OR m.memberStatus = :memberStatus)")
    List<Member> searchMembers(
            @Param("parkingLotId") Long parkingLotId,
            @Param("phoneNumber") String phoneNumber,
            @Param("memberCode") String memberCode,
            @Param("email") String email,
            @Param("keyword") String keyword,
            @Param("memberStatus") MemberStatus memberStatus
    );

    /**
     * Count members by status
     */
    Long countByMemberStatus(MemberStatus status);

    /**
     * Count members by parking plan price unit
     */
    @Query("SELECT COUNT(m) FROM Member m JOIN m.parkingPlan p WHERE p.priceUnit = :priceUnit")
    Long countByParkingPlanPriceUnit(@Param("priceUnit") String priceUnit);

    /**
     * Count members by parking lot
     */
    Long countByParkingLotId(Long parkingLotId);

    /**
     * Count active members by parking lot
     */
    Long countByParkingLotIdAndMemberStatus(Long parkingLotId, MemberStatus status);
}

