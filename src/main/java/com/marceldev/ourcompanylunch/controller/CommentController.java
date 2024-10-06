package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.comment.CommentOutputDto;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentRequest;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentResponse;
import com.marceldev.ourcompanylunch.dto.comment.GetCommentListRequest;
import com.marceldev.ourcompanylunch.dto.comment.UpdateCommentRequest;
import com.marceldev.ourcompanylunch.service.CommentService;
import com.marceldev.ourcompanylunch.service.MessageProducerService;
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
@Tag(name = "4 Comment")
public class CommentController {

  private final CommentService commentService;

  private final MessageProducerService messageProducerService;

  @Operation(
      summary = "Write a comment on a diner",
      description = "A member can write a comment on a registered diner.<br>"
          + "Also enter a sharing option."
  )
  @PostMapping("/diners/{id}/comments")
  public ResponseEntity<CreateCommentResponse> createComment(
      @PathVariable long id,
      @Validated @RequestBody CreateCommentRequest dto
  ) {
    CreateCommentResponse response = commentService.createComment(id, dto);
    messageProducerService.produceForDinerSubscribers(id, dto.getContent());
    return ResponseEntity.ok(response);
  }

  @Operation(
      summary = "Get a list of comments on the diner",
      description =
          "A member can get a list of comments on the diner that shared in the company.<br>"
              + "Query with author name, comment content text. Sort by created time."
  )
  @GetMapping("/diners/{id}/comments")
  public ResponseEntity<Page<CommentOutputDto>> getCommentList(
      @PathVariable long id,
      @Validated @ModelAttribute GetCommentListRequest getCommentListRequest
  ) {
    Page<CommentOutputDto> comments = commentService.getCommentList(
        id, getCommentListRequest
    );
    return ResponseEntity.ok(comments);
  }

  @Operation(
      summary = "Update the comment",
      description = "A member can update the comment that he/she wrote."
  )
  @PutMapping("/diners/comments/{id}")
  public ResponseEntity<Void> updateComment(
      @PathVariable long id,
      @RequestBody UpdateCommentRequest updateCommentRequest
  ) {
    commentService.updateComment(id, updateCommentRequest);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "Remove the comment",
      description = "A member can remove the comment that he/she wrote."
  )
  @DeleteMapping("/diners/comments/{id}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable long id
  ) {
    commentService.deleteComment(id);
    return ResponseEntity.ok().build();
  }
}
