package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.reply.CreateReplyDto;
import com.marceldev.companylunchcomment.dto.reply.ReplyOutputDto;
import com.marceldev.companylunchcomment.dto.reply.UpdateReplyDto;
import com.marceldev.companylunchcomment.response.CustomResponse;
import com.marceldev.companylunchcomment.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "Reply", description = "댓글 관련")
public class ReplyController {

  private final ReplyService replyService;

  @Operation(
      summary = "댓글 작성",
      description = "사용자는 코멘트에 댓글을 작성할 수 있다."
  )
  @PostMapping("/diners/{dinerId}/comments/{commentsId}/replies")
  public CustomResponse<?> createReply(
      @PathVariable long commentsId,
      @Validated @RequestBody CreateReplyDto createReplyDto
  ) {
    replyService.createReply(commentsId, createReplyDto);
    return CustomResponse.success();
  }

  @Operation(
      summary = "댓글 조회",
      description = "사용자는 코멘트에 작성된 댓글을 조회할 수 있다."
  )
  @GetMapping("/diners/{dinerId}/comments/{commentsId}/replies")
  public CustomResponse<?> getReplyList(
      @PathVariable long commentsId,
      Pageable pageable
  ) {
    Page<ReplyOutputDto> replies = replyService.getReplyList(commentsId, pageable);
    return CustomResponse.success(replies);
  }

  @Operation(
      summary = "댓글 수정",
      description = "사용자는 코멘트에 댓글 수정이 가능하다.<br>"
          + "수정은 자신이 작성한 댓글만 가능하다."
  )
  @PostMapping("/diners/{dinerId}/comments/{commentsId}/replies/{replyId}")
  public CustomResponse<?> updateReply(
      @PathVariable long replyId,
      @Validated @RequestBody UpdateReplyDto updateReplyDto
  ) {
    replyService.updateReply(replyId, updateReplyDto);
    return CustomResponse.success();
  }

  @Operation(
      summary = "댓글 삭제",
      description = "사용자는 댓글 삭제가 가능하다.<br>"
          + "삭제는 자신이 작성한 댓글만 가능하다."
  )
  @DeleteMapping("/diners/{dinerId}/comments/{commentsId}/replies/{replyId}")
  public CustomResponse<?> deleteReply(
      @PathVariable long replyId
  ) {
    replyService.deleteReply(replyId);
    return CustomResponse.success();
  }
}
