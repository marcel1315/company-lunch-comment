package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.component.NotificationProvider;
import com.marceldev.ourcompanylunch.dto.comment.CommentOutputDto;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentDto;
import com.marceldev.ourcompanylunch.dto.comment.GetCommentListDto;
import com.marceldev.ourcompanylunch.dto.comment.UpdateCommentDto;
import com.marceldev.ourcompanylunch.service.CommentService;
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
@Tag(name = "4 Comment", description = "코멘트 관련")
public class CommentController {

  private final CommentService commentService;

  private final NotificationProvider notificationProvider;

  @Operation(
      summary = "식당에 코멘트 작성",
      description = "사용자는 등록된 식당에 대해 코멘트를 작성할 수 있다.<br>"
          + "식당, 코멘트 내용, 사내 공유 여부를 입력한다."
  )
  @PostMapping("/diners/{id}/comments")
  public ResponseEntity<Void> createComment(
      @PathVariable long id,
      @Validated @RequestBody CreateCommentDto createCommentDto
  ) {
    commentService.createComment(id, createCommentDto);
    notificationProvider.enqueueMessages(id, createCommentDto.getContent());
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "식당의 코멘트 조회",
      description = "사용자는 사내 공유된 식당의 코멘트 목록을 조회할 수 있다.<br>"
          + "작성자 이름, 코멘트 내용으로 목록을 조회할 수 있다. 작성시간순으로 정렬할 수 있다."
  )
  @GetMapping("/diners/{id}/comments")
  public ResponseEntity<Page<CommentOutputDto>> getCommentList(
      @PathVariable long id,
      @Validated @ModelAttribute GetCommentListDto getCommentListDto
  ) {
    Page<CommentOutputDto> comments = commentService.getCommentList(
        id, getCommentListDto
    );
    return ResponseEntity.ok(comments);
  }

  @Operation(
      summary = "코멘트 수정",
      description = "사용자는 자신이 작성한 코멘트를 수정할 수 있다."
  )
  @PutMapping("/diners/comments/{id}")
  public ResponseEntity<Void> updateComment(
      @PathVariable long id,
      @RequestBody UpdateCommentDto updateCommentDto
  ) {
    commentService.updateComment(id, updateCommentDto);
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "코멘트 삭제",
      description = "사용자는 자신이 작성한 코멘트를 삭제할 수 있다."
  )
  @DeleteMapping("/diners/comments/{id}")
  public ResponseEntity<Void> deleteComment(
      @PathVariable long id
  ) {
    commentService.deleteComment(id);
    return ResponseEntity.ok().build();
  }
}