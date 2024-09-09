package com.marceldev.ourcompanylunch.component;

import com.marceldev.ourcompanylunch.dto.comment.NotificationMessage;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.PushNotificationToken;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProviderZZ {

  private final static String CHECKOUT_COMPLETE_TOPIC_NAME = "checkout.complete.v1";

  private final KafkaTemplate<String, String> kafkaTemplate;

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  private final DinerSubscriptionRepository dinerSubscriptionRepository;

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

    kafkaTemplate.send(CHECKOUT_COMPLETE_TOPIC_NAME, messageContent);
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
        .orElseThrow(MemberNotFoundException::new);
  }

  private Company getCompany() {
    Member member = getMember();
    if (member.getCompany() == null) {
      throw new CompanyNotFoundException();
    }
    return member.getCompany();
  }

  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
