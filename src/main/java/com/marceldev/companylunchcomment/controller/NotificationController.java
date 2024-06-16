package com.marceldev.companylunchcomment.controller;

import com.marceldev.companylunchcomment.response.CustomResponse;
import com.marceldev.companylunchcomment.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
public class NotificationController {

  private final NotificationService notificationService;

  @Operation(
      summary = "SSE 커넥션 맺기",
      description = "최대 연결시간은 30분이다. 30분이 지나서 끊기면 재연결이 필요하다."
  )
  @GetMapping("/notification/sse")
  public SseEmitter notificationSse() {
    return notificationService.createEmitter();
  }

  @Operation(
      summary = "SSE 커넥션 끊기",
      description = "클라이언트가 더 이상 연결하지 않으려면 명시적으로 끊어야 한다.<br>"
          + "서버에서 keep-alive를 15초 간격으로 호출해서 끊긴 것을 감지하지만, 15초 동안 있다고 가정하고 SSE 알림을 시도한다."
  )
  @PostMapping("/notification/sse/terminate")
  public CustomResponse<?> notificationSseTerminate() {
    notificationService.removeEmitter();
    return CustomResponse.success();
  }

  @Operation(
      summary = "FCM Push Notification Token 전달",
      description = "클라이언트가 발급받은 FCM Push Notificaton Token을 서버로 전달한다."
  )
  @PostMapping("/notification/fcm/token")
  public CustomResponse<?> notificationFcmToken(@RequestBody String token) {
    notificationService.registerToken(token);
    return CustomResponse.success();
  }

  @Operation(
      summary = "FCM Push Notification Token 삭제",
      description = "클라이언트가 발급받은 FCM Push Notification Token을 서버에서 삭제한다."
  )
  @DeleteMapping("/notification/fcm/token")
  public CustomResponse<?> notificationFcmTokenDelete() {
    notificationService.unregisterToken();
    return CustomResponse.success();
  }
}
