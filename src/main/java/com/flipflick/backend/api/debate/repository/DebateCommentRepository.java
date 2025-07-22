package com.flipflick.backend.api.debate.repository;

import com.flipflick.backend.api.debate.entity.DebateComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DebateCommentRepository extends JpaRepository<DebateComment,Long> {
    List<DebateComment> findByDebateIdAndIsDeletedFalseOrderByCreatedAtAsc(Long debateId);

    @Query("SELECT COUNT(dc) FROM DebateComment dc WHERE dc.debate.id = :debateId AND dc.isDeleted = false")
    int countByDebateId(@Param("debateId") Long debateId);
}
