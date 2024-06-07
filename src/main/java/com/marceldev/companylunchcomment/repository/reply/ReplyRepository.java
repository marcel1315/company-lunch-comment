package com.marceldev.companylunchcomment.repository.reply;

import com.marceldev.companylunchcomment.entity.Reply;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<Reply, Long> {

  Page<Reply> findByCommentsId(long commentsId, Pageable pageable);
}
