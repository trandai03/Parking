package com.project.parking.service;

import com.project.parking.dto.RevenueStatDTO;
import com.project.parking.exceptions.DataNotFoundException;
import com.project.parking.model.ParkingLot;
import com.project.parking.model.ParkingSession;
import com.project.parking.model.RevenueStat;
import com.project.parking.repository.ParkingLotRepository;
import com.project.parking.repository.ParkingSessionRepository;
import com.project.parking.repository.RevenueStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RevenueStatService {

    private final RevenueStatRepository revenueStatRepository;
    private final ParkingLotRepository parkingLotRepository;
    private final ParkingSessionRepository parkingSessionRepository;

    public List<RevenueStatDTO> getAllRevenueStats(Long parkingLotId, LocalDate startDate, LocalDate endDate) {
        return revenueStatRepository.findAll(parkingLotId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public RevenueStatDTO getRevenueStatById(Long id) throws DataNotFoundException {
        RevenueStat revenueStat = revenueStatRepository.findById(id)
                .orElseThrow(() -> new DataNotFoundException("Revenue statistic not found with id: " + id));
        return convertToDTO(revenueStat);
    }

    public List<RevenueStatDTO> getRevenueStatsByParkingLot(Long parkingLotId) {
        return revenueStatRepository.findByParkingLotId(parkingLotId).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RevenueStatDTO> getRevenueStatsByDateRange(LocalDate startDate, LocalDate endDate) {
        return revenueStatRepository.findByDateBetween(startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<RevenueStatDTO> getRevenueStatsByParkingLotAndDateRange(Long parkingLotId, LocalDate startDate, LocalDate endDate) {
        return revenueStatRepository.findByParkingLotIdAndDateBetween(parkingLotId, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    @Scheduled(cron = "0 5 0 * * ?") // Chạy vào 00:05 mỗi ngày
    public void generateDailyRevenueStats() {
        log.info("Generating daily revenue statistics...");
        LocalDate yesterday = LocalDate.now().minusDays(1);
        generateRevenueStatsForDate(yesterday);
    }

    @Transactional
    public void generateRevenueStatsForDate(LocalDate date) {
        LocalDateTime startDateTime = date.atStartOfDay();
        LocalDateTime endDateTime = date.atTime(LocalTime.MAX);

        // Lấy danh sách tất cả các bãi đỗ xe
        List<ParkingLot> parkingLots = parkingLotRepository.findAll();
        
        for (ParkingLot parkingLot : parkingLots) {
            // Kiểm tra xem đã tồn tại thống kê cho bãi đỗ xe và ngày này chưa
            if (revenueStatRepository.existsByParkingLotIdAndDate(parkingLot.getId(), date)) {
                log.info("Revenue statistics already exist for parking lot {} on date {}", parkingLot.getId(), date);
                continue;
            }
            
            // Lấy tất cả các phiên gửi xe hoàn thành trong ngày cho bãi đỗ xe này
            List<ParkingSession> completedSessions = parkingSessionRepository.findByLotIdAndExitTimeBetween(
                    parkingLot.getId(), startDateTime, endDateTime);
            
            if (completedSessions.isEmpty()) {
                log.info("No completed parking sessions for parking lot {} on date {}", parkingLot.getId(), date);
                continue;
            }
            
            int totalSessions = completedSessions.size();
            
            // Tính tổng doanh thu từ các phiên gửi xe
            BigDecimal totalRevenue = BigDecimal.ZERO;
            for (ParkingSession session : completedSessions) {
                totalRevenue = totalRevenue.add(session.getTotalCost());
            }
            
            // Tính thời gian trung bình
            long totalDurationMinutes = 0;
            for (ParkingSession session : completedSessions) {
                if (session.getEntryTime() != null && session.getExitTime() != null) {
                    long durationMinutes = Duration.between(session.getEntryTime(), session.getExitTime()).toMinutes();
                    totalDurationMinutes += durationMinutes;
                }
            }
            int averageDurationMinutes = totalSessions > 0 ? (int) (totalDurationMinutes / totalSessions) : 0;
            
            // Tạo và lưu thống kê doanh thu
            RevenueStat revenueStat = new RevenueStat();
            revenueStat.setLot(parkingLot);
            revenueStat.setStatDate(date);
            revenueStat.setTotalSessions(totalSessions);
            revenueStat.setTotalRevenue(totalRevenue);
            revenueStat.setAverageDurationMinutes(averageDurationMinutes);
            revenueStat.setCreatedAt(LocalDateTime.now());

            revenueStatRepository.save(revenueStat);
            log.info("Generated revenue statistics for parking lot {} on date {}: total sessions={}, total revenue={}, average duration={}",
                    parkingLot.getId(), date, totalSessions, totalRevenue, averageDurationMinutes);
        }
    }

    private RevenueStatDTO convertToDTO(RevenueStat revenueStat) {
        return RevenueStatDTO.builder()
                .id(revenueStat.getId())
                .parkingLotId(revenueStat.getLot().getId())
                .parkingLotName(revenueStat.getLot().getName())
                .date(revenueStat.getStatDate())
                .totalSessions(revenueStat.getTotalSessions())
                .totalRevenue(revenueStat.getTotalRevenue())
                .averageDurationMinutes(revenueStat.getAverageDurationMinutes())
                .build();
    }
}