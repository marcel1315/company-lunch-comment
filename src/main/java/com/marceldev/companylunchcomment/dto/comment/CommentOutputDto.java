package com.marceldev.companylunchcomment.dto.comment;

import com.marceldev.companylunchcomment.entity.Comment;
import com.marceldev.companylunchcomment.type.ShareStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class CommentOutputDto {

  private long id;
  private String content;
  private ShareStatus shareStatus;
  private LocalDateTime createdAt;
  private long commentedById;
  private String commentedByName;
  private long dinerId;

  public CommentOutputDto(Long id, String content, ShareStatus shareStatus,
      LocalDateTime createdAt,
      long commentedById, String commentedByName, long dinerId) {
    this.id = id;
    this.content = content;
    this.shareStatus = shareStatus;
    this.createdAt = createdAt;
    this.commentedById = commentedById;
    this.commentedByName = commentedByName;
    this.dinerId = dinerId;
  }

  public static CommentOutputDto of(Comment comment, String name) {
    return CommentOutputDto.builder()
        .id(comment.getId())
        .content(comment.getContent())
        .shareStatus(comment.getShareStatus())
        .createdAt(comment.getCreatedAt())
        .commentedById(comment.getMember().getId())
        .commentedByName(name)
        .dinerId(comment.getDiner().getId())
        .build();
  }
}
