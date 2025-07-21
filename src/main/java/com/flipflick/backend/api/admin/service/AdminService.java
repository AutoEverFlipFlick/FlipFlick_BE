package com.flipflick.backend.api.admin.service;

import com.flipflick.backend.api.admin.dto.*;
import com.flipflick.backend.api.debate.repository.DebateRepository;
import com.flipflick.backend.api.member.dto.MemberListResponseDto;
import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.report.entity.Report;
import com.flipflick.backend.api.report.repository.ReportRepository;
import com.flipflick.backend.api.review.repository.ReviewRepository;
import com.flipflick.backend.common.exception.BadRequestException;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final MemberRepository memberRepository;
    private final ReviewRepository reviewRepository;
    private final ReportRepository reportRepository;
    private final DebateRepository debateRepository;

    public DashboardStatResponseDto getDashboardStats() {
        Map<String, Map<String, List<TimeSeriesData>>> stats = new HashMap<>();

        stats.put("user", Map.of(
                "7D", getUserStats(7, 1),
                "30D", getUserStats(30, 5),
                "90D", getUserStats(90, 10)
        ));

        stats.put("review", Map.of(
                "7D", getReviewStats(7, 1),
                "30D", getReviewStats(30, 5),
                "90D", getReviewStats(90, 10)
        ));

        stats.put("debate", Map.of(
                "7D", getDebateStats(7, 1),
                "30D", getDebateStats(30, 5),
                "90D", getDebateStats(90, 10)
        ));


        return new DashboardStatResponseDto(stats);
    }

    private List<TimeSeriesData> getReviewStats(int days, int interval) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> newDataRows = reviewRepository.countNewReviewsByDate(startDate, endDate);
        Map<LocalDate, Long> newDataMap = toMap(newDataRows);

        List<Object[]> allDataRows = reviewRepository.countReviewsUntilDate(endDate);
        Map<LocalDate, Long> cumulativeMap = buildCumulativeMap(allDataRows, startDate, endDate);

        List<TimeSeriesData> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean isIntervalDay = (days == 7) || (date.getDayOfMonth() % interval == startDate.getDayOfMonth() % interval);


            if (isIntervalDay || date.equals(endDate)) {
                Long newCount = newDataMap.getOrDefault(date, 0L);
                Long totalCount = cumulativeMap.getOrDefault(date, 0L);
                result.add(new TimeSeriesData(date, newCount, totalCount));
            }
        }

        return result;
    }

    private List<TimeSeriesData> getUserStats(int days, int interval) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> newDataRows = memberRepository.countNewMembersByDate(startDate, endDate);
        Map<LocalDate, Long> newDataMap = toMap(newDataRows);

        List<Object[]> allDataRows = memberRepository.countMembersUntilDate(endDate);
        Map<LocalDate, Long> cumulativeMap = buildCumulativeMap(allDataRows, startDate, endDate);

        List<TimeSeriesData> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean isIntervalDay = (days == 7) || (date.getDayOfMonth() % interval == startDate.getDayOfMonth() % interval);


            if (isIntervalDay || date.equals(endDate)) {
                Long newCount = newDataMap.getOrDefault(date, 0L);
                Long totalCount = cumulativeMap.getOrDefault(date, 0L);
                result.add(new TimeSeriesData(date, newCount, totalCount));
            }
        }

        return result;
    }

    private List<TimeSeriesData> getDebateStats(int days, int interval) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1);

        List<Object[]> newDataRows = debateRepository.countNewDebatesByDate(startDate, endDate);
        Map<LocalDate, Long> newDataMap = toMap(newDataRows);

        List<Object[]> allDataRows = debateRepository.countDebatesUntilDate(endDate);
        Map<LocalDate, Long> cumulativeMap = buildCumulativeMap(allDataRows, startDate, endDate);

        List<TimeSeriesData> result = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            boolean isIntervalDay = (days == 7) || (date.getDayOfMonth() % interval == startDate.getDayOfMonth() % interval);

            if (isIntervalDay || date.equals(endDate)) {
                Long newCount = newDataMap.getOrDefault(date, 0L);
                Long totalCount = cumulativeMap.getOrDefault(date, 0L);
                result.add(new TimeSeriesData(date, newCount, totalCount));
            }
        }

        return result;
    }

    private Map<LocalDate, Long> toMap(List<Object[]> rows) {
        Map<LocalDate, Long> map = new HashMap<>();
        for (Object[] row : rows) {
            map.put(((java.sql.Date) row[0]).toLocalDate(), ((Number) row[1]).longValue());
        }
        return map;
    }

    private Map<LocalDate, Long> buildCumulativeMap(List<Object[]> rawRows, LocalDate startDate, LocalDate endDate) {
        Map<LocalDate, Long> dailyMap = toMap(rawRows);
        Map<LocalDate, Long> cumulativeMap = new TreeMap<>();
        long cumulativeSum = 0;


        for (Map.Entry<LocalDate, Long> entry : dailyMap.entrySet()) {
            if (entry.getKey().isBefore(startDate)) {
                cumulativeSum += entry.getValue();
            }
        }

        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            cumulativeSum += dailyMap.getOrDefault(date, 0L);
            cumulativeMap.put(date, cumulativeSum);
        }

        return cumulativeMap;
    }

    public List<PopcornGradeResponseDto> getUserCountByPopcornGrade() {
        List<Object[]> rows = memberRepository.countMembersByPopcornGrade();

        return rows.stream()
                .map(row -> new PopcornGradeResponseDto((String) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    public List<MovieReviewCountResponseDto> getTop5MoviesByReviewCount() {
        Pageable topFive = PageRequest.of(0, 5);
        return reviewRepository.findTop5MoviesByReviewCount(topFive);
    }

    @Transactional
    public void updateMemberStatus(Long memberId, String status) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.USER_NOT_FOUND.getMessage()));

        switch (status) {
            case "경고":
                member.addWarning();
                break;

            case "정지":
                member.suspend();
                break;

            case "차단":
                member.blockPermanently();
                break;

            case "해제":
                member.unblock();
                break;

            default:
                throw new BadRequestException(ErrorStatus.INVALID_STATUS.getMessage());
        }
    }

    public Page<MemberListResponseDto> getMembersWithStats(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Member> members;

        if (keyword != null && !keyword.isBlank()) {
            members = memberRepository.findByNicknameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                    keyword, keyword, pageable
            );
        } else {
            members = memberRepository.findAll(pageable);
        }

        return members.map(member -> {
            int reviewCount = reviewRepository.countByMember(member);
            int postCount = 0;
            return MemberListResponseDto.from(member, reviewCount, postCount);
        });
    }

    @Transactional
    public void processReport(Long reportId,ReportActionRequestDto request) {
        Report report = reportRepository.findById(reportId)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REPORT_NOT_FOUND.getMessage()));

        Member target = report.getTarget();

        if (report.isHandled()) {
            throw new BadRequestException(ErrorStatus.ALREADY_REPORT.getMessage());
        }

        switch (request.getAction()) {
            case "경고" -> target.addWarning();
            case "정지" -> target.suspend();
            case "차단" -> target.blockPermanently();
            case "기각" -> {} // 아무것도 안함
            default -> throw new BadRequestException(ErrorStatus.INVALID_REPORT_ACTION.getMessage());
        }

        report.markAsHandled();
    }

    public Page<ReportAdminResponseDto> getReportsWithFilter(int page, int size, String keyword, String status) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Report> reports = reportRepository.findReportsFiltered(keyword, status, pageable);

        return reports.map(ReportAdminResponseDto::from);
    }

}
