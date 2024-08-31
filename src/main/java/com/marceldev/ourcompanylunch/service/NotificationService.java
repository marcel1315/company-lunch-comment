package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.component.FCMPushNotification;
import com.marceldev.ourcompanylunch.config.RabbitMQConfig;
import com.marceldev.ourcompanylunch.dto.comment.NotificationMessage;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.PushNotificationToken;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.repository.pushnotificatontoken.PushNotificationTokenRepository;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

  private final PushNotificationTokenRepository pushNotificationTokenRepository;

  private final MemberRepository memberRepository;

  private final ConcurrentHashMap<Long, SseEmitter> memberEmitters = new ConcurrentHashMap<>();

  private final ConcurrentHashMap<Long, Boolean> memberOnline = new ConcurrentHashMap<>();

  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

  private final FCMPushNotification fcmPushNotification;

  /**
   * 클라이언트가 SSE 연결을 시작할 때 호출하는 부분
   */
  public SseEmitter createEmitter() {
    long memberId = getMember().getId();

    SseEmitter emitter = new SseEmitter(1800_000L); // 30 * 60 * 1000 ms = 30 minutes
    memberEmitters.put(memberId, emitter);
    memberOnline.put(memberId, true);

    emitter.onCompletion(() -> {
      removeMemberFromSseConnection(memberId);
      log.debug("MemberId: {}. Connection completed.", memberId);
    });
    emitter.onTimeout(() -> {
      removeMemberFromSseConnection(memberId);
      log.debug("MemberId: {}. Connection timeout.", memberId);
    });
    emitter.onError((e) -> {
      removeMemberFromSseConnection(memberId);
    });

    // Send keep-alive message every 15 seconds to check whether client is disconnected.
    scheduler.scheduleAtFixedRate(() -> {
      try {
        emitter.send(SseEmitter.event().name("keep-alive").data("keep-alive"));
        log.debug("MemberId: {}. Sent keep-alive", memberId);
      } catch (IOException e) {
        emitter.completeWithError(e);
      }
    }, 0, 15, TimeUnit.SECONDS);

    return emitter;
  }

  /**
   * 클라이언트가 SSE 연결을 끊을 때 호출하는 부분
   */
  public void removeEmitter() {
    long memberId = getMember().getId();

    SseEmitter emitter = memberEmitters.get(memberId);
    if (emitter != null) {
      emitter.complete();
      // memberEmitters와 memberOnline 배열의 값은 complete에 넣어준 함수로 제거될 것임
    }
  }

  /**
   * 회원별 FCM Token을 저장해놓기
   */
  public void registerToken(String token) {
    pushNotificationTokenRepository.findByMember(getMember())
        .ifPresent(pushNotificationTokenRepository::delete);

    PushNotificationToken tokenEntity = PushNotificationToken.builder()
        .token(token)
        .member(getMember())
        .build();
    pushNotificationTokenRepository.save(tokenEntity);
  }

  /**
   * 회원의 FCM Token을 제거
   */
  public void unregisterToken() {
    pushNotificationTokenRepository.findByMember(getMember())
        .ifPresent(pushNotificationTokenRepository::delete);
  }

  /**
   * Queue에 들어온 메시지를 처리함. 사용자가 SSE 연결이 되어 있으면 SSE 응답으로 보내기. SSE 연결이 없으면, Push Notification으로 보내기
   * listener를 실행하는 시점에 SecurityContextHolder는 유지되지 않음.
   */
  @RabbitListener(queues = RabbitMQConfig.QUEUE_NAME, containerFactory = "rabbitListenerContainerFactory")
  private void receiveMessage(NotificationMessage message) {
    log.debug("receiveMessage: " + message);

    long receiverId = message.getReceiverId();
    String body = message.getDinerName() + ": " + message.getSenderName() + ": \n"
        + message.getContent();

    if (memberOnline.getOrDefault(receiverId, false)) {
      sendSseNotification(receiverId, body);
    } else if (message.getReceiverFcmToken() != null) {
      sendPushNotification(message.getReceiverFcmToken(), body);
    } else {
      log.warn("Tried to send notifications, but couldn't. ReceiverId: {}", receiverId);
    }
  }

  /**
   * SSE로 클라이언트에게 응답 보내기
   */
  private void sendSseNotification(long memberId, String messageContent) {
    SseEmitter emitter = memberEmitters.get(memberId);
    if (emitter != null) {
      try {
        emitter.send(SseEmitter.event().name("message").data(messageContent));
      } catch (IOException e) {
        removeMemberFromSseConnection(memberId);
      }
    }
  }

  /**
   * FCM Push Notification 보내기
   */
  private void sendPushNotification(String token, String messageContent) {
    fcmPushNotification.sendPushNotification(token, messageContent);
  }

  private void removeMemberFromSseConnection(long memberId) {
    memberEmitters.remove(memberId);
    memberOnline.remove(memberId);
  }

  private Member getMember() {
    String email = getMemberEmail();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }

  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
