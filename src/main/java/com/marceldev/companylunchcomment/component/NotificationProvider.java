package com.marceldev.companylunchcomment.component;

import com.marceldev.companylunchcomment.config.RabbitMQConfig;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
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

  public void enqueueMessage(String message) {
    String messageWithMemberId = getMember().getId() + ":" + message;

    rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, messageWithMemberId);

    log.info("Notification enqueued: {}", messageWithMemberId);
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
