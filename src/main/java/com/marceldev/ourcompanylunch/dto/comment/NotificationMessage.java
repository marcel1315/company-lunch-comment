package com.marceldev.ourcompanylunch.dto.comment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotificationMessage {

  private Long senderId;
  private Long receiverId;
  private Long dinerId;
  private String senderName;
  private String dinerName;
  private String content;
  private String receiverFcmToken;
}
