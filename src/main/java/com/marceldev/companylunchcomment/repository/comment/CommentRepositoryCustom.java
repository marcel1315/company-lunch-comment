package com.marceldev.companylunchcomment.repository.comment;

import com.marceldev.companylunchcomment.dto.comment.GetCommentListDto;
import com.marceldev.companylunchcomment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

  Page<Comment> getList(GetCommentListDto dto, long myMemberId, long dinerId, Pageable pageable);
}
