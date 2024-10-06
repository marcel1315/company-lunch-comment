package com.marceldev.ourcompanylunch.repository.comment;

import com.marceldev.ourcompanylunch.dto.comment.GetCommentListRequest;
import com.marceldev.ourcompanylunch.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentRepositoryCustom {

  Page<Comment> getList(GetCommentListRequest dto, long myMemberId, long dinerId,
      Pageable pageable);
}
