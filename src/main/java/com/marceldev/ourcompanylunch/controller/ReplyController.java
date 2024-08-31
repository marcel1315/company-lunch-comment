package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.reply.CreateReplyDto;
import com.marceldev.ourcompanylunch.dto.reply.GetReplyListDto;
import com.marceldev.ourcompanylunch.dto.reply.ReplyOutputDto;
import com.marceldev.ourcompanylunch.dto.reply.UpdateReplyDto;
import com.marceldev.ourcompanylunch.service.ReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Tag(name = "5 Reply", description = "댓글 관련")
public class ReplyController {

  private final ReplyService replyService;

  @Operation(
      summary = "댓글 작성",
      description = "사용자는 코멘트에 댓글을 작성할 수 있다."
  )
  @PostMapping("/comments/{id}/replies")
  public ResponseEntity<Void> createReply(
      @PathVariable long id,
      @Validated @RequestBody CreateReplyDto createReplyDto
  ) {
    replyService.createReply(id, createReplyDto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "댓글 조회",
      description = "사용자는 코멘트에 작성된 댓글을 조회할 수 있다."
  )
  @GetMapping("/comments/{id}/replies")
  public ResponseEntity<Page<ReplyOutputDto>> getReplyList(
      @PathVariable long id,
      @Validated @ModelAttribute GetReplyListDto getReplyListDto
  ) {
    Page<ReplyOutputDto> replies = replyService.getReplyList(id, getReplyListDto);
    return ResponseEntity.ok(replies);
  }

  @Operation(
      summary = "댓글 수정",
      description = "사용자는 코멘트에 댓글 수정이 가능하다.<br>"
          + "수정은 자신이 작성한 댓글만 가능하다."
  )
  @PutMapping("comments/replies/{id}")
  public ResponseEntity<Void> updateReply(
      @PathVariable long id,
      @Validated @RequestBody UpdateReplyDto updateReplyDto
  ) {
    replyService.updateReply(id, updateReplyDto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "댓글 삭제",
      description = "사용자는 댓글 삭제가 가능하다.<br>"
          + "삭제는 자신이 작성한 댓글만 가능하다."
  )
  @DeleteMapping("/comments/replies/{id}")
  public ResponseEntity<Void> deleteReply(
      @PathVariable long id
  ) {
    replyService.deleteReply(id);
    return ResponseEntity.ok().build();
  }
}