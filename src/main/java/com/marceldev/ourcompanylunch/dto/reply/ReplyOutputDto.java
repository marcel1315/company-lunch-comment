package com.marceldev.ourcompanylunch.dto.reply;

import com.marceldev.ourcompanylunch.entity.Reply;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReplyOutputDto {

  private Long id;
  private String content;
  private String memberName;
  private LocalDateTime createdAt;
  private long repliedBy;

  public static ReplyOutputDto of(Reply reply) {
    return ReplyOutputDto.builder()
        .id(reply.getId())
        .content(reply.getContent())
        .memberName(reply.getMember().getName())
        .createdAt(reply.getCreatedAt())
        .repliedBy(reply.getMember().getId())
        .build();
  }
}
