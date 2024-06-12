package com.marceldev.companylunchcomment.repository.comment;

import com.marceldev.companylunchcomment.entity.Comment;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long>,
    CommentRepositoryCustom {

  Optional<Comment> findByIdAndMember_Email(long commentId, String email);
}
