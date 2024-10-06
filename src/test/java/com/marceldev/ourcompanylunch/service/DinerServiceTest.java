package com.marceldev.ourcompanylunch.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.marceldev.ourcompanylunch.component.S3Manager;
import com.marceldev.ourcompanylunch.dto.diner.AddDinerTagsDto;
import com.marceldev.ourcompanylunch.dto.diner.RemoveDinerTagsDto;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.DinerSubscription;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.diner.AlreadySubscribedException;
import com.marceldev.ourcompanylunch.exception.diner.DinerSubscriptionNotFoundException;
import com.marceldev.ourcompanylunch.repository.diner.DinerImageRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerSubscriptionRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class DinerServiceTest {

  @Mock
  private DinerRepository dinerRepository;

  @Mock
  private DinerImageRepository dinerImageRepository;

  @Mock
  private MemberRepository memberRepository;

  @Mock
  private DinerSubscriptionRepository dinerSubscriptionRepository;

  @Mock
  private S3Manager s3Manager;

  @InjectMocks
  private DinerService dinerService;

  // Mock company
  // When retrieving diner, if it's not the diner in the company of the member, the diner is not accessible.
  Company company1 = Company.builder()
      .id(1L)
      .name("HelloCompany")
      .address("123, Gangnam-daero Gangnam-gu Seoul")
      .location(LocationUtil.createPoint(37.123123, 127.123123))
      .enterKey("company123")
      .build();

  // Mock member
  Member member1 = Member.builder()
      .id(1L)
      .email("jack@example.com")
      .name("Jack")
      .role(Role.VIEWER)
      .company(company1)
      .build();

  // Mock diner
  Diner diner1 = Diner.builder()
      .id(1L)
      .name("Gamsung Taco")
      .link("taco.com")
      .location(LocationUtil.createPoint(37.123123, 127.123123))
      .company(company1)
      .tags(new LinkedHashSet<>(List.of("tag1", "tag2")))
      .build();

  @BeforeEach
  public void setupMember() {
    GrantedAuthority authority = new SimpleGrantedAuthority("VIEWER");
    Authentication authentication = new UsernamePasswordAuthenticationToken(member1.getEmail(),
        null, List.of(authority));

    SecurityContext securityContext = mock(SecurityContext.class);
    lenient().when(securityContext.getAuthentication()).thenReturn(authentication);

    SecurityContextHolder.setContext(securityContext);

    lenient().when(memberRepository.findByEmail(any()))
        .thenReturn(Optional.of(member1));

    lenient().when(dinerRepository.findById(any()))
        .thenReturn(Optional.of(diner1));
  }

  @AfterEach
  public void clearSecurityContext() {
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("Add diner tag - Success(When tags is empty)")
  void test_update_diner_add_tag_blank() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("tag1", "tag2"));

    //when
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .company(company1)
                .tags(new LinkedHashSet<>())
                .build()
        ));

    //then
    dinerService.addDinerTag(1, dto);
  }

  @Test
  @DisplayName("Add diner tag - Success(When other tags exists. Existing tags should remain.)")
  void test_update_diner_add_tag_not_blank() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("tag2", "tag3"));

    //when
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .company(company1)
                .tags(new LinkedHashSet<>(List.of("tag1")))
                .build()
        ));

    //then
    dinerService.addDinerTag(1, dto);
  }

  @Test
  @DisplayName("Add diner tag - Success(Same name tags already exists. Add only non-existing ones and success.)")
  void test_update_diner_add_tag_already_exist_tag() {
    //given
    AddDinerTagsDto dto = new AddDinerTagsDto();
    dto.setTags(List.of("tag1", "tag2"));

    //when
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .company(company1)
                .tags(new LinkedHashSet<>(List.of("tag2")))
                .build()
        ));

    //then
    dinerService.addDinerTag(1, dto);
  }

  @Test
  @DisplayName("Add diner tag - Success")
  void test_update_diner_remove_tag() {
    //given
    RemoveDinerTagsDto dto = new RemoveDinerTagsDto();
    dto.setTags(List.of("tag1", "tag2"));

    //when
    when(dinerRepository.findById(anyLong()))
        .thenReturn(Optional.ofNullable(
            Diner
                .builder()
                .id(1L)
                .company(company1)
                .tags(new LinkedHashSet<>(List.of("tag1", "tag2", "tag3")))
                .build()
        ));

    //then
    dinerService.removeDinerTag(1, dto);
  }

  @Test
  @DisplayName("Subscribe diner - Success")
  void subscribe_diner() {
    //given
    //when
    when(dinerSubscriptionRepository.existsByDinerAndMember(any(), any()))
        .thenReturn(false);

    dinerService.subscribeDiner(1L);
    ArgumentCaptor<DinerSubscription> captor = ArgumentCaptor.forClass(DinerSubscription.class);

    //then
    verify(dinerSubscriptionRepository).save(captor.capture());
    assertEquals(1L, captor.getValue().getDiner().getId());
  }

  @Test
  @DisplayName("Subscribe diner - Fail(Already subscribing)")
  void subscribe_diner_fail_already_subscribed() {
    //given
    //when
    when(dinerSubscriptionRepository.existsByDinerAndMember(any(), any()))
        .thenReturn(true);

    //then
    assertThrows(AlreadySubscribedException.class,
        () -> dinerService.subscribeDiner(1L));
  }

  @Test
  @DisplayName("Unsubscribe diner - Success")
  void unsubscribe_diner() {
    //given
    //when
    when(dinerSubscriptionRepository.findByDinerAndMember(any(), any()))
        .thenReturn(Optional.of(DinerSubscription.builder()
            .id(1L)
            .build()));

    dinerService.unsubscribeDiner(1L);

    //then
    verify(dinerSubscriptionRepository).delete(any());
  }

  @Test
  @DisplayName("Unsubscribe diner - Fail(No subscription)")
  void unsubscribe_diner_fail_no_subscription() {
    //given
    //when
    when(dinerSubscriptionRepository.findByDinerAndMember(any(), any()))
        .thenReturn(Optional.empty());

    //then
    assertThrows(DinerSubscriptionNotFoundException.class,
        () -> dinerService.unsubscribeDiner(1L));
  }
}