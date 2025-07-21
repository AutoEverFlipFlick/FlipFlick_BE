package com.flipflick.backend.api.report.service;

import com.flipflick.backend.api.member.entity.Member;
import com.flipflick.backend.api.member.repository.MemberRepository;
import com.flipflick.backend.api.report.dto.ReportRequestDto;
import com.flipflick.backend.api.report.entity.Report;
import com.flipflick.backend.api.report.repository.ReportRepository;
import com.flipflick.backend.common.exception.NotFoundException;
import com.flipflick.backend.common.response.ErrorStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final MemberRepository memberRepository;
    private final ReportRepository reportRepository;

    @Transactional
    public void createReport(ReportRequestDto dto) {
        Member reporter = memberRepository.findById(dto.getReporterId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REPORTER_NOT_FOUND.getMessage()));
        Member target = memberRepository.findById(dto.getTargetId())
                .orElseThrow(() -> new NotFoundException(ErrorStatus.TARGET_NOT_FOUND.getMessage()));

        Report report = Report.builder()
                .reporter(reporter)
                .target(target)
                .type(dto.getType())
                .content(dto.getContent())
                .targetTitle(dto.getTargetTitle())
                .targetContent(dto.getTargetContent())
                .targetEntityId(dto.getTargetEntityId())
                .build();

        reportRepository.save(report);
    }

    @Transactional
    public void deleteReport(Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorStatus.REPORT_NOT_FOUND.getMessage()));

        reportRepository.delete(report);
    }
}