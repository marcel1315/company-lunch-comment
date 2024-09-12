package com.marceldev.ourcompanylunch.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.marceldev.ourcompanylunch.dto.comment.MessageCommentWriteV1;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageProducerService {

  private final static String COMMENT_WRITE_TOPIC_NAME = "comment.write.v1";

  private final KafkaTemplate<String, String> kafkaTemplate;

  private final MemberRepository memberRepository;

  private final DinerRepository dinerRepository;

  private final DinerSubscriptionRepository dinerSubscriptionRepository;

  private final ObjectMapper objectMapper;

  /**
   * Produce messages to kafka for the diner subscriptions.
   */
  public void produceForDinerSubscribers(long dinerId, String messageContent) {
    Member sender = getMember();
    Diner diner = getDiner(dinerId);

    Set<DinerSubscription> subscriptions = dinerSubscriptionRepository.findDinerSubscriptionByDinerId(
        dinerId);

    // No need to send message to self.
    subscriptions.removeIf(s -> s.getMember().getId().equals(sender.getId()));

    subscriptions.stream()
        .map(s -> MessageCommentWriteV1.builder()
            .senderId(sender.getId())
            .receiverId(s.getMember().getId())
            .dinerId(diner.getId())
            .senderName(sender.getName())
            .receiverName(s.getMember().getName())
            .dinerName(diner.getName())
            .content(messageContent)
            .build())
        .map(this::convertMessageToString)
        .forEach(m -> kafkaTemplate.send(COMMENT_WRITE_TOPIC_NAME, m));
  }

  private String convertMessageToString(MessageCommentWriteV1 dto) {
    try {
      return objectMapper.writeValueAsString(dto);
    } catch (JsonProcessingException e) {
      log.error(e.getMessage());
      throw new RuntimeException();
    }
  }

  private String getEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }

  private Diner getDiner(long dinerId) {
    Company company = getCompany();
    return dinerRepository.findById(dinerId)
        .filter((diner) -> diner.getCompany().equals(company))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));
  }

  private Member getMember() {
    String email = getEmail();
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
}
