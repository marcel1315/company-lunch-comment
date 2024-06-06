package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.dto.reply.CreateReplyDto;
import com.marceldev.companylunchcomment.response.CustomResponse;
import com.marceldev.companylunchcomment.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReplyController {

  private final ReplyService replyService;

  @Operation(
      summary = "댓글 작성",
      description = "사용자는 코멘트에 댓글을 작성할 수 있다."
  )
  @PostMapping("/diner/{dinerId}/comments/{commentsId}/reply")
  public CustomResponse<?> createReply(
      @PathVariable long commentsId,
      @Validated @RequestBody CreateReplyDto createReplyDto,
      Authentication auth
  ) {
    replyService.createReply(commentsId, createReplyDto, auth.getName());
    return CustomResponse.success();
  }
}
