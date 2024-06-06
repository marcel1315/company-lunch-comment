package com.marceldev.companylunchcomment.dto.comments;

import java.time.LocalDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentsOutputDto {

  private long id;
  private String content;
  private String shareStatus;
  private LocalDateTime createdAt;
  private long commentedBy;
  private long dinerId;

  public CommentsOutputDto(Long id, String content, String shareStatus, LocalDateTime createdAt,
      long commentedBy, long dinerId) {
    this.id = id;
    this.content = content;
    this.shareStatus = shareStatus;
    this.createdAt = createdAt;
    this.commentedBy = commentedBy;
    this.dinerId = dinerId;
  }
}
