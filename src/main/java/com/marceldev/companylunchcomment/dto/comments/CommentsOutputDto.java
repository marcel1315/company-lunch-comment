package com.marceldev.companylunchcomment.dto.comments;

import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.type.ShareStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class CommentsOutputDto {

  private long id;
  private String content;
  private ShareStatus shareStatus;
  private LocalDateTime createdAt;
  private long commentedById;
  private String commentedByName;
  private long dinerId;

  public CommentsOutputDto(Long id, String content, ShareStatus shareStatus,
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

  public static CommentsOutputDto of(Comments comments, String name) {
    return CommentsOutputDto.builder()
        .id(comments.getId())
        .content(comments.getContent())
        .shareStatus(comments.getShareStatus())
        .createdAt(comments.getCreatedAt())
        .commentedById(comments.getMember().getId())
        .commentedByName(name)
        .dinerId(comments.getDiner().getId())
        .build();
  }
}
