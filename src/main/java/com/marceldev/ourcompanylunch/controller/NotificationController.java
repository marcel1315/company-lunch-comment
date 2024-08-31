package com.marceldev.ourcompanylunch.controller;

import com.marceldev.ourcompanylunch.dto.notification.RegisterFcmToken;
import com.marceldev.ourcompanylunch.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@Tag(name = "6 Notification", description = "알림 관련")
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(
      summary = "SSE 커넥션 맺기",
      description = "최대 연결시간은 30분이다. 30분이 지나서 끊기면 재연결이 필요하다."
  )
  @GetMapping("/notifications/sse")
  public SseEmitter notificationSse() {
    return notificationService.createEmitter();
  }

  @Operation(
      summary = "SSE 커넥션 끊기",
      description = "클라이언트가 더 이상 연결하지 않으려면 명시적으로 끊어야 한다.<br>"
          + "서버에서 keep-alive를 15초 간격으로 호출해서 끊긴 것을 감지하지만, 15초 동안 있다고 가정하고 SSE 알림을 시도한다."
  )
  @PostMapping("/notifications/sse/terminate")
  public ResponseEntity<Void> notificationSseTerminate() {
    notificationService.removeEmitter();
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "FCM Push Notification Token 전달",
      description = "클라이언트가 발급받은 FCM Push Notificaton Token을 서버로 전달한다."
  )
  @PostMapping("/notifications/fcm/token")
  public ResponseEntity<Void> notificationFcmToken(
      @RequestBody RegisterFcmToken token
  ) {
    notificationService.registerToken(token.getToken());
    return ResponseEntity.ok().build();
  }

  @Operation(
      summary = "FCM Push Notification Token 삭제",
      description = "클라이언트가 발급받은 FCM Push Notification Token을 서버에서 삭제한다."
  )
  @DeleteMapping("/notifications/fcm/token")
  public ResponseEntity<Void> notificationFcmTokenDelete() {
    notificationService.unregisterToken();
    return ResponseEntity.ok().build();
  }
}
