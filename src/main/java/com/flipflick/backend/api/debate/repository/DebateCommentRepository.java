package com.flipflick.backend.api.debate.repository;

import com.flipflick.backend.api.debate.entity.DebateComment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DebateCommentRepository extends JpaRepository<DebateComment,Long> {
    List<DebateComment> findByDebateIdAndIsDeletedFalseOrderByCreatedAtAsc(Long debateId);
}
