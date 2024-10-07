package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.marceldev.ourcompanylunch.basic.IntegrationTest;
import com.marceldev.ourcompanylunch.dto.member.UpdateMemberRequest;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.member.MemberUnauthorizedException;
import com.marceldev.ourcompanylunch.type.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MemberServiceTest extends IntegrationTest {

  @Test
  @DisplayName("Update member - Success")
  void update_member_info() {
    // given
    Member member = saveMember();
    UpdateMemberRequest request = UpdateMemberRequest.create("James");

    // when
    memberService.updateMember(member.getId(), request);

    // then
    Member savedMember = memberRepository.findById(member.getId()).orElseThrow();
    assertThat(savedMember.getName()).isEqualTo("James");
  }

  @Test
  @DisplayName("Update member - Fail(member id is not own id - Unauthorized)")
  void update_member_info_fail() {
    // given
    UpdateMemberRequest request = UpdateMemberRequest.create("James");

    // when // then
    assertThatThrownBy(() -> memberService.updateMember(1L, request))
        .isInstanceOf(MemberUnauthorizedException.class);
  }

  // --- Save some entity ---

  private Member saveMember() {
    Member member = Member.builder()
        .name("Jack")
        .email("jack@example.com")
        .company(null)
        .role(Role.VIEWER)
        .build();
    return memberRepository.save(member);
  }
}