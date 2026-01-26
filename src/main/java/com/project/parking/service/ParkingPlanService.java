package com.project.parking.service;

import com.project.parking.dto.ParkingPlanDTO;
import com.project.parking.dto.request.CreateParkingPlanRequest;
import com.project.parking.dto.request.UpdateParkingPlanRequest;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.ParkingPlan;
import com.project.parking.repository.ParkingLotRepository;
import com.project.parking.repository.ParkingPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ParkingPlanService {

    private final ParkingPlanRepository parkingPlanRepository;
    private final ParkingLotRepository parkingLotRepository;

    /**
     * Get all plans by parking lot
     */
    @Transactional(readOnly = true)
    public List<ParkingPlanDTO> getPlansByParkingLot(Long parkingLotId) {
        log.info("Getting plans for parking lot: {}", parkingLotId);
        List<ParkingPlan> plans = parkingPlanRepository.findActivePlansByParkingLotOrdered(parkingLotId);
        return plans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get all plans (including inactive) by parking lot - for admin
     */
    @Transactional(readOnly = true)
    public List<ParkingPlanDTO> getAllPlansByParkingLot(Long parkingLotId) {
        log.info("Getting all plans for parking lot: {}", parkingLotId);
        List<ParkingPlan> plans = parkingPlanRepository.findByParkingLotId(parkingLotId);
        return plans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Get plan by ID
     */
    @Transactional(readOnly = true)
    public ParkingPlanDTO getPlanById(Long id) throws DataNotFoundException {
        log.info("Getting plan with id: {}", id);
        ParkingPlan plan = parkingPlanRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Gói không tồn tại với ID: " + id));
        return convertToDTO(plan);
    }

    /**
     * Create new parking plan
     */
    @Transactional
    public ParkingPlanDTO createPlan(CreateParkingPlanRequest request) throws DataNotFoundException {
        log.info("Creating new parking plan: {}", request.getName());

        // Validate parking lot exists
        ParkingLot parkingLot = parkingLotRepository.findById(request.getParkingLotId())
                .orElseThrow(() -> new DataNotFoundException("Bãi đỗ xe không tồn tại với ID: " + request.getParkingLotId()));

        // Check if plan name already exists for this parking lot
        if (parkingPlanRepository.existsByParkingLotIdAndName(request.getParkingLotId(), request.getName())) {
            throw new DataIntegrityViolationException("Tên gói đã tồn tại trong bãi đỗ xe này");
        }

        ParkingPlan plan = ParkingPlan.builder()
                .parkingLot(parkingLot)
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .priceUnit(request.getPriceUnit().toUpperCase())
                .planType(request.getPlanType().toUpperCase())
                .isUnlimitedParking(request.getIsUnlimitedParking() != null ? request.getIsUnlimitedParking() : false)
                .hasFixedSpot(request.getHasFixedSpot() != null ? request.getHasFixedSpot() : false)
                .hasValetService(request.getHasValetService() != null ? request.getHasValetService() : false)
                .hasCarWash(request.getHasCarWash() != null ? request.getHasCarWash() : false)
                .hasCoveredParking(request.getHasCoveredParking() != null ? request.getHasCoveredParking() : false)
                .hasSecurity247(request.getHasSecurity247() != null ? request.getHasSecurity247() : false)
                .isPopular(request.getIsPopular() != null ? request.getIsPopular() : false)
                .isActive(true)
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .build();

        ParkingPlan savedPlan = parkingPlanRepository.save(plan);
        log.info("Created new parking plan with id: {}", savedPlan.getId());

        return convertToDTO(savedPlan);
    }

    /**
     * Update parking plan
     */
    @Transactional
    public ParkingPlanDTO updatePlan(Long id, UpdateParkingPlanRequest request) throws DataNotFoundException {
        log.info("Updating parking plan with id: {}", id);

        ParkingPlan plan = parkingPlanRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Gói không tồn tại với ID: " + id));

        // Update fields if provided
        if (request.getName() != null) {
            // Check if new name already exists (but not for current plan)
            if (!plan.getName().equals(request.getName()) && 
                parkingPlanRepository.existsByParkingLotIdAndName(plan.getParkingLot().getId(), request.getName())) {
                throw new DataIntegrityViolationException("Tên gói đã tồn tại trong bãi đỗ xe này");
            }
            plan.setName(request.getName());
        }

        if (request.getDescription() != null) {
            plan.setDescription(request.getDescription());
        }

        if (request.getPrice() != null) {
            plan.setPrice(request.getPrice());
        }

        if (request.getPriceUnit() != null) {
            plan.setPriceUnit(request.getPriceUnit().toUpperCase());
        }

        if (request.getPlanType() != null) {
            plan.setPlanType(request.getPlanType().toUpperCase());
        }

        if (request.getIsUnlimitedParking() != null) {
            plan.setIsUnlimitedParking(request.getIsUnlimitedParking());
        }

        if (request.getHasFixedSpot() != null) {
            plan.setHasFixedSpot(request.getHasFixedSpot());
        }

        if (request.getHasValetService() != null) {
            plan.setHasValetService(request.getHasValetService());
        }

        if (request.getHasCarWash() != null) {
            plan.setHasCarWash(request.getHasCarWash());
        }

        if (request.getHasCoveredParking() != null) {
            plan.setHasCoveredParking(request.getHasCoveredParking());
        }

        if (request.getHasSecurity247() != null) {
            plan.setHasSecurity247(request.getHasSecurity247());
        }

        if (request.getIsPopular() != null) {
            plan.setIsPopular(request.getIsPopular());
        }

        if (request.getIsActive() != null) {
            plan.setIsActive(request.getIsActive());
        }

        if (request.getSortOrder() != null) {
            plan.setSortOrder(request.getSortOrder());
        }

        ParkingPlan updatedPlan = parkingPlanRepository.save(plan);
        log.info("Updated parking plan with id: {}", id);

        return convertToDTO(updatedPlan);
    }

    /**
     * Delete parking plan (soft delete - set inactive)
     */
    @Transactional
    public void deletePlan(Long id) throws DataNotFoundException {
        log.info("Deleting parking plan with id: {}", id);

        ParkingPlan plan = parkingPlanRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Gói không tồn tại với ID: " + id));

        plan.setIsActive(false);
        parkingPlanRepository.save(plan);
        log.info("Soft deleted parking plan with id: {}", id);
    }

    /**
     * Hard delete parking plan
     */
    @Transactional
    public void hardDeletePlan(Long id) throws DataNotFoundException {
        log.info("Hard deleting parking plan with id: {}", id);

        if (!parkingPlanRepository.existsById(id)) {
            throw new DataNotFoundException("Gói không tồn tại với ID: " + id);
        }

        parkingPlanRepository.deleteById(id);
        log.info("Hard deleted parking plan with id: {}", id);
    }

    /**
     * Get popular plans
     */
    @Transactional(readOnly = true)
    public List<ParkingPlanDTO> getPopularPlans() {
        log.info("Getting popular plans");
        List<ParkingPlan> plans = parkingPlanRepository.findByIsPopularTrueAndIsActiveTrue();
        return plans.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /**
     * Convert entity to DTO
     */
    private ParkingPlanDTO convertToDTO(ParkingPlan plan) {
        return ParkingPlanDTO.builder()
                .id(plan.getId())
                .parkingLotId(plan.getParkingLot().getId())
                .parkingLotName(plan.getParkingLot().getName())
                .name(plan.getName())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .priceUnit(plan.getPriceUnit())
                .planType(plan.getPlanType())
                .isUnlimitedParking(plan.getIsUnlimitedParking())
                .hasFixedSpot(plan.getHasFixedSpot())
                .hasValetService(plan.getHasValetService())
                .hasCarWash(plan.getHasCarWash())
                .hasCoveredParking(plan.getHasCoveredParking())
                .hasSecurity247(plan.getHasSecurity247())
                .isPopular(plan.getIsPopular())
                .isActive(plan.getIsActive())
                .sortOrder(plan.getSortOrder())
                .createdAt(plan.getCreatedAt())
                .updatedAt(plan.getUpdatedAt())
                .build();
    }
}

