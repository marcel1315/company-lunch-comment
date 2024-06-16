package com.marceldev.companylunchcomment.dto.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationMessage {

  private Long memberId;
  private String content;
  private String fcmToken;
}
