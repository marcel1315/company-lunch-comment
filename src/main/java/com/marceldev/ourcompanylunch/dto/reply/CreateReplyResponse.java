package com.marceldev.ourcompanylunch.dto.reply;

import com.marceldev.ourcompanylunch.entity.Reply;
import lombok.Builder;
import lombok.Getter;

@Getter
public class CreateReplyResponse {

  private final Long id;
  private final String content;

  @Builder
  private CreateReplyResponse(Long id, String content) {
    this.id = id;
    this.content = content;
  }

  public static CreateReplyResponse create(String content) {
    return CreateReplyResponse.builder()
        .content(content)
        .build();
  }

  public static CreateReplyResponse of(Reply reply) {
    return CreateReplyResponse.builder()
        .id(reply.getId())
        .content(reply.getContent())
        .build();
  }
}