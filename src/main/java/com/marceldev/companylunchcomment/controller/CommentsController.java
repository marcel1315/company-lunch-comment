package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.comments.CommentsOutputDto;
import com.marceldev.companylunchcomment.dto.comments.CreateCommentDto;
import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import com.marceldev.companylunchcomment.response.CustomResponse;
import com.marceldev.companylunchcomment.service.CommentsService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CommentsController {

  private final CommentsService commentsService;

  @Operation(
      summary = "식당에 코멘트 작성",
      description = "사용자는 등록된 식당에 대해 코멘트를 작성할 수 있다.<br>"
          + "식당, 코멘트 내용, 사내 공유 여부를 입력한다."
  )
  @PostMapping("/diner/{dinerId}/comments")
  public CustomResponse<?> createComments(
      @PathVariable long dinerId,
      @Validated @RequestBody CreateCommentDto createCommentDto,
      Authentication auth
  ) {
    commentsService.createComment(dinerId, createCommentDto, auth.getName());
    return CustomResponse.success();
  }

  @Operation(
      summary = "식당의 코멘트 조회",
      description = "사용자는 사내 공유된 식당의 코멘트 목록을 조회할 수 있다.<br>"
          + "작성자 이름, 코멘트 내용으로 목록을 조회할 수 있다. 작성시간순으로 정렬할 수 있다."
  )
  @GetMapping("/diner/{dinerId}/comments")
  public CustomResponse<?> getCommentsList(
      @PathVariable long dinerId,
      @Validated GetCommentsListDto getCommentsListDto,
      Authentication auth
  ) {
    Page<CommentsOutputDto> comments = commentsService.getCommentsList(
        dinerId, auth, getCommentsListDto
    );
    return CustomResponse.success(comments);
  }
}
