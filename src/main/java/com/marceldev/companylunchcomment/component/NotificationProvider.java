package com.marceldev.companylunchcomment.component;

import com.marceldev.companylunchcomment.config.RabbitMQConfig;
import com.marceldev.companylunchcomment.dto.comment.NotificationMessage;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.PushNotificationToken;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import com.marceldev.companylunchcomment.repository.pushnotificatontoken.PushNotificationTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProvider {

  private final RabbitTemplate rabbitTemplate;

  private final MemberRepository memberRepository;

  private final PushNotificationTokenRepository pushNotificationTokenRepository;

  public void enqueueMessage(String messageContent) {
    Member member = getMember();
    String token = pushNotificationTokenRepository.findByMember(member)
        .map(PushNotificationToken::getToken)
        .orElse(null);
    NotificationMessage message = NotificationMessage.builder()
        .memberId(member.getId())
        .content(messageContent)
        .fcmToken(token)
        .build();

    rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);

    log.info("Notification enqueued: {}", message);
  }

  private Member getMember() {
    String email = getMemberEmail();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);
  }

  private String getMemberEmail() {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return user.getUsername();
  }
}
