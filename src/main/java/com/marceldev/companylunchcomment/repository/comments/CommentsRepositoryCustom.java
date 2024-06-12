package com.marceldev.companylunchcomment.repository.comments;

import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import com.marceldev.companylunchcomment.entity.Comments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentsRepositoryCustom {

  Page<Comments> getList(GetCommentsListDto dto, long myMemberId, long dinerId, Pageable pageable);
}
