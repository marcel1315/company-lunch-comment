package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.reply.CreateReplyRequest;
import com.marceldev.ourcompanylunch.dto.reply.CreateReplyResponse;
import com.marceldev.ourcompanylunch.dto.reply.GetReplyListRequest;
import com.marceldev.ourcompanylunch.dto.reply.ReplyOutputDto;
import com.marceldev.ourcompanylunch.dto.reply.UpdateReplyRequest;
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
@Tag(name = "5 Reply")
public class ReplyController {

  private final ReplyService replyService;

  @Operation(
      summary = "Write a reply",
      description = "A member can write a reply on the comment."
  )
  @PostMapping("/comments/{id}/replies")
  public ResponseEntity<CreateReplyResponse> createReply(
      @PathVariable long id,
      @Validated @RequestBody CreateReplyRequest dto
  ) {
    CreateReplyResponse response = replyService.createReply(id, dto);
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get a list of reply",
      description = "A member can get a list of reply on the comment."
  )
  @GetMapping("/comments/{id}/replies")
  public ResponseEntity<Page<ReplyOutputDto>> getReplyList(
      @PathVariable long id,
      @Validated @ModelAttribute GetReplyListRequest getReplyListRequest
  ) {
    Page<ReplyOutputDto> replies = replyService.getReplyList(id, getReplyListRequest);
    return ResponseEntity.ok(replies);
  }

  @Operation(
      summary = "Update a reply",
      description = "A member can change a reply that he/she wrote."
  )
  @PutMapping("comments/replies/{id}")
  public ResponseEntity<Void> updateReply(
      @PathVariable long id,
      @Validated @RequestBody UpdateReplyRequest updateReplyRequest
  ) {
    replyService.updateReply(id, updateReplyRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Remove a reply",
      description = "A member can remove a reply that he/she wrote."
  )
  @DeleteMapping("/comments/replies/{id}")
  public ResponseEntity<Void> deleteReply(
      @PathVariable long id
  ) {
    replyService.deleteReply(id);
    return ResponseEntity.ok().build();
  }
}
