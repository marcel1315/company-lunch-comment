package com.marceldev.ourcompanylunch.repository.reply;

import com.marceldev.ourcompanylunch.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  Page<Reply> findByCommentIdOrderByCreatedAtDesc(long commentId, Pageable pageable);
}
