package com.marceldev.ourcompanylunch.dto.comment;

import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.type.ShareStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateCommentResponse {

  private final Long id;
  private final String content;
  private final ShareStatus shareStatus;

  @Builder
  private CreateCommentResponse(Long id, String content, ShareStatus shareStatus) {
    this.id = id;
    this.content = content;
    this.shareStatus = shareStatus;
  }

  public static CreateCommentResponse of(Comment comment) {
    return CreateCommentResponse.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .shareStatus(comment.getShareStatus())
        .build();
  }
}