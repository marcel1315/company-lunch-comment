package com.marceldev.companylunchcomment.mapper;

import com.marceldev.companylunchcomment.dto.comments.CommentsOutputDto;
import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper
public interface CommentsMapper {

  long selectListCount(long dinerId, String myMemberName, GetCommentsListDto getCommentsListDto);

  List<CommentsOutputDto> selectList(long dinerId, String myMemberName,
      GetCommentsListDto getCommentsListDto, Pageable pageable, String sort);
}
