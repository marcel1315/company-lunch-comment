package com.marceldev.companylunchcomment.dto.comments;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CommentsOutputDto {

  private long id;
  private String content;
  private String shareStatus;
  private LocalDateTime createdAt;
  private long commentedBy;
  private long dinerId;
}
