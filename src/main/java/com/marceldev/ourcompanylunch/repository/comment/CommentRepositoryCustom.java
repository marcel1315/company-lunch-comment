package com.marceldev.ourcompanylunch.repository.comment;

import com.marceldev.ourcompanylunch.dto.comment.GetCommentListDto;
import com.marceldev.ourcompanylunch.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

  Page<Comment> getList(GetCommentListDto dto, long myMemberId, long dinerId, Pageable pageable);
}
