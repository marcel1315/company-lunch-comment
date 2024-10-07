package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.marceldev.ourcompanylunch.basic.IntegrationTest;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentRequest;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentResponse;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.dto.reply.CreateReplyRequest;
import com.marceldev.ourcompanylunch.dto.reply.CreateReplyResponse;
import com.marceldev.ourcompanylunch.dto.reply.GetReplyListRequest;
import com.marceldev.ourcompanylunch.dto.reply.ReplyOutputDto;
import com.marceldev.ourcompanylunch.dto.reply.UpdateReplyRequest;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.entity.Reply;
import com.marceldev.ourcompanylunch.exception.company.CompanyNotFoundException;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.ShareStatus;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

class ReplyServiceTest extends IntegrationTest {

  @Test
  @DisplayName("Create reply - Success")
  void create_reply() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    Comment comment = saveComment(diner, "It's delicious");

    CreateReplyRequest request = CreateReplyRequest.builder()
        .content("I'll try.")
        .build();

    // when
    CreateReplyResponse response = replyService.createReply(comment.getId(), request);

    // then
    Reply reply = replyRepository.findById(response.getId()).orElseThrow();
    assertThat(reply)
        .extracting("content", "comment.id")
        .containsExactly("I'll try.", comment.getId());
  }

  @Test
  @DisplayName("Create reply - Fail(Company with the comment not found)")
  void create_reply_fail_no_comments() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    // Not saving comment

    CreateReplyRequest request = CreateReplyRequest.builder()
        .content("I'll try.")
        .build();

    // when // then
    assertThatThrownBy(() -> replyService.createReply(1L, request))
        .isInstanceOf(CompanyNotFoundException.class);
  }

  @Test
  @DisplayName("Get reply list - Success")
  void get_reply_list() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    Comment comment = saveComment(diner, "It's delicious");

    CreateReplyRequest createReplyRequest = CreateReplyRequest.create("I'll try");
    CreateReplyResponse createReplyResponse = replyService.createReply(comment.getId(),
        createReplyRequest);

    GetReplyListRequest request = GetReplyListRequest.create();

    // when
    Page<ReplyOutputDto> replyList = replyService.getReplyList(comment.getId(),
        request);

    // then
    assertThat(replyList.getContent())
        .extracting("content", "memberName")
        .containsExactly(
            tuple("I'll try", "Jack")
        );
  }

  @Test
  @DisplayName("Update reply - Success")
  void update_reply() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    Comment comment = saveComment(diner, "It's delicious");

    CreateReplyRequest createReplyRequest = CreateReplyRequest.create("I'll try");
    CreateReplyResponse createReplyResponse = replyService.createReply(comment.getId(),
        createReplyRequest);

    UpdateReplyRequest request = UpdateReplyRequest.create("I'll try next time.");

    // when
    replyService.updateReply(createReplyResponse.getId(), request);

    // then
    Reply reply = replyRepository.findById(createReplyResponse.getId()).orElseThrow();
    assertThat(reply.getContent()).isEqualTo("I'll try next time.");
  }

  @Test
  @DisplayName("Update reply - Fail(Company with that reply not found)")
  void update_reply_fail_no_reply() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    saveComment(diner, "It's delicious");

    // Not saving reply

    UpdateReplyRequest request = UpdateReplyRequest.create("I'll try next time.");

    // when // then
    assertThatThrownBy(() -> replyService.updateReply(1L, request))
        .isInstanceOf(CompanyNotFoundException.class);
  }

  @Test
  @DisplayName("Delete reply - Success")
  void delete_reply() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    Comment comment = saveComment(diner, "It's delicious");

    CreateReplyRequest createReplyRequest = CreateReplyRequest.create("I'll try");
    CreateReplyResponse createReplyResponse = replyService.createReply(comment.getId(),
        createReplyRequest);

    //when
    replyService.deleteReply(createReplyResponse.getId());

    //then
    Optional<Reply> reply = replyRepository.findById(createReplyResponse.getId());
    assertThat(reply).isEmpty();
  }

  @Test
  @DisplayName("Delete reply - Fail(Company with that reply not found)")
  void delete_reply_fail_no_reply() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner();
    Comment comment = saveComment(diner, "It's delicious");

    // Not saving reply

    // when // then
    assertThatThrownBy(() -> replyService.deleteReply(1L))
        .isInstanceOf(CompanyNotFoundException.class);
  }

  // --- Save some entity ---

  private Company saveCompany() {
    Company company = Company.builder()
        .name("HelloCompany")
        .address("321, Teheran-ro Gangnam-gu Seoul")
        .enterKey("company123")
        .enterKeyEnabled(false)
        .location(LocationUtil.createPoint(37.123456, 127.123456))
        .build();
    return companyRepository.save(company);
  }

  private Member saveMember() {
    Member member = Member.builder()
        .name("Jack")
        .email("jack@example.com")
        .company(null)
        .role(Role.VIEWER)
        .build();
    return memberRepository.save(member);
  }

  private void chooseCompany(Company company) {
    ChooseCompanyRequest chooseCompanyRequest = new ChooseCompanyRequest("company123");
    companyService.chooseCompany(company.getId(), chooseCompanyRequest);
  }

  private Diner saveDiner() {
    CreateDinerRequest request = CreateDinerRequest.builder()
        .name("HotTaco")
        .link("diner.com")
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(new LinkedHashSet<>())
        .build();
    CreateDinerResponse response = dinerService.createDiner(request);
    return dinerRepository.findById(response.getId()).orElseThrow();
  }

  private Comment saveComment(Diner diner, String content) {
    CreateCommentRequest createCommentRequest = CreateCommentRequest.create(content,
        ShareStatus.COMPANY);
    CreateCommentResponse createCommentResponse = commentService.createComment(diner.getId(),
        createCommentRequest);
    return commentRepository.findById(createCommentResponse.getId()).orElseThrow();
  }
}