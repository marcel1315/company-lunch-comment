package com.marceldev.companylunchcomment.repository;

import com.marceldev.companylunchcomment.dto.comments.CommentsOutputDto;
import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CommentsRepositoryCustom {

  Page<CommentsOutputDto> getList(GetCommentsListDto condition, Pageable pageable);
}
