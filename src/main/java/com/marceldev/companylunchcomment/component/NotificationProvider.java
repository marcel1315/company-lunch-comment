package com.marceldev.companylunchcomment.component;

import com.marceldev.companylunchcomment.config.RabbitMQConfig;
import com.marceldev.companylunchcomment.dto.comment.NotificationMessage;
import com.marceldev.companylunchcomment.entity.Company;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.DinerSubscription;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.entity.PushNotificationToken;
import com.marceldev.companylunchcomment.exception.company.CompanyNotExistException;
import com.marceldev.companylunchcomment.exception.diner.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.member.MemberNotExistException;
import com.marceldev.companylunchcomment.exception.notification.FailToEnqueueNotificationsException;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerSubscriptionRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProvider {

  private final RabbitTemplate rabbitTemplate;

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  private final DinerSubscriptionRepository dinerSubscriptionRepository;

  /**
   * 각자에게 도착할 메시지를 enqueue할 때, 1000명에게 도달하려면 1000개의 메시지를 만들어 큐에 넣는 방식이 있고, 1개의 메시지를 만들고 consumer에서
   * 1000개의 알림을 보내는 방식이 있다. 여기서는 1000개의 메시지를 만들어 큐에 넣음
   */
  // TODO: SSE 연결이 아닌 사람은 FCM의 topic 개념을 사용하면 일일이 token을 지정해 보내지 않을 수 있음
  public void enqueueMessages(long dinerId, String messageContent) {
    Member sender = getMember();
    Diner diner = getDiner(dinerId);

    // 항상 FCM만 사용하는 것은 아니지만, 여기서 token을 조회해서 큐에 넣어주는 이유는
    // consumer에서 token을 일일이 조회하지 않도록 하기 위해
    List<DinerSubscription> subscriptions = dinerSubscriptionRepository.findDinerSubscriptionAndTokenByDinerId(
        dinerId);
    // 본인에게 보내는 알림 메시지는 불필요
    //subscriptions.removeIf(s -> s.getMember().getId().equals(sender.getId()));

    List<NotificationMessage> messages = subscriptions.stream()
        .map(s -> NotificationMessage.builder()
            .senderId(sender.getId())
            .receiverId(s.getMember().getId())
            .dinerId(diner.getId())
            .senderName(sender.getName())
            .dinerName(diner.getName())
            .content(messageContent)
            .receiverFcmToken(Optional.ofNullable(s.getMember().getToken())
                .map(PushNotificationToken::getToken)
                .orElse(null))
            .build())
        .toList();

    rabbitTemplate.execute(channel -> {
      try {
        channel.txSelect();
        for (NotificationMessage message : messages) {
          rabbitTemplate.convertAndSend(RabbitMQConfig.QUEUE_NAME, message);
        }
        channel.txCommit();
        log.info("{} notifications enqueued.", messages.size());
      } catch (Exception e) {
        channel.txRollback();
        throw new FailToEnqueueNotificationsException();
      }
      return null;
    });
  }

  private Diner getDiner(long dinerId) {
    Company company = getCompany();
    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().equals(company))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  private Member getMember() {
    String email = getMemberEmail();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);
  }

  private Company getCompany() {
    Member member = getMember();
    if (member.getCompany() == null) {
      throw new CompanyNotExistException();
    }
    return member.getCompany();
  }

  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
