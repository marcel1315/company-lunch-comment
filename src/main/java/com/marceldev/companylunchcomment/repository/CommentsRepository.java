package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.entity.Comments;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentsRepository extends JpaRepository<Comments, Long> {

}
