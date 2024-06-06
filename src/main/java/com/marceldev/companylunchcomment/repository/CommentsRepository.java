package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Comments;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments, Long>,
    CommentsRepositoryCustom {

  Optional<Comments> findByIdAndMember_Email(long commentsId, String email);
}
