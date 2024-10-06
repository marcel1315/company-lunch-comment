package com.marceldev.ourcompanylunch.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.marceldev.ourcompanylunch.basic.IntegrationTest;
import com.marceldev.ourcompanylunch.dto.comment.CommentOutputDto;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentRequest;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentResponse;
import com.marceldev.ourcompanylunch.dto.comment.GetCommentListRequest;
import com.marceldev.ourcompanylunch.dto.comment.UpdateCommentRequest;
import com.marceldev.ourcompanylunch.dto.company.ChooseCompanyRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerRequest;
import com.marceldev.ourcompanylunch.dto.diner.CreateDinerResponse;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.entity.Company;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.comment.CommentNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.type.Role;
import com.marceldev.ourcompanylunch.type.ShareStatus;
import com.marceldev.ourcompanylunch.util.LocationUtil;
import java.util.LinkedHashSet;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;

class CommentServiceTest extends IntegrationTest {

  @Test
  @DisplayName("Create comment - Success")
  void create_comment() {
    // given
    Company company = saveCompany();
    Member member = saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    CreateCommentRequest request = CreateCommentRequest.builder()
        .content("It's delicious")
        .shareStatus(ShareStatus.COMPANY)
        .build();

    // when
    CreateCommentResponse response = commentService.createComment(diner.getId(), request);

    // then
    Comment comment = commentRepository.findById(response.getId()).orElseThrow();
    assertThat(comment)
        .extracting("content", "shareStatus", "diner.id", "member.id")
        .containsExactly(
            "It's delicious", ShareStatus.COMPANY, diner.getId(), member.getId()
        );
  }

  @Test
  @DisplayName("Create comment - Fail(Diner not found)")
  void create_comment_fail_diner_not_found() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    // Not saving diner

    CreateCommentRequest request = createCreateCommentRequest("It's delicious");

    // when // then
    assertThrows(DinerNotFoundException.class,
        () -> commentService.createComment(1L, request));
  }

  @Test
  @DisplayName("Get comment list - Success")
  void get_comment_list() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    CreateCommentRequest createCommentRequest1 = createCreateCommentRequest("It's delicious");
    CreateCommentRequest createCommentRequest2 = createCreateCommentRequest("Very kind");
    commentService.createComment(diner.getId(), createCommentRequest1);
    commentService.createComment(diner.getId(), createCommentRequest2);

    GetCommentListRequest request = GetCommentListRequest.create(
        null, "Jack"
    );

    // when
    Page<CommentOutputDto> commentsPage = commentService.getCommentList(diner.getId(), request);

    // then
    assertThat(commentsPage.getContent()).hasSize(2)
        .extracting("content", "shareStatus", "commentedByName")
        .containsExactlyInAnyOrder(
            tuple("It's delicious", ShareStatus.COMPANY, "Jack"),
            tuple("Very kind", ShareStatus.COMPANY, "Jack")
        );
  }

  @Test
  @DisplayName("Update comment - Success")
  void update_comment() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    CreateCommentRequest createCommentRequest = createCreateCommentRequest("It's delicious");
    CreateCommentResponse createCommentResponse = commentService.createComment(diner.getId(),
        createCommentRequest);

    UpdateCommentRequest request = UpdateCommentRequest.create(
        "Great meal", ShareStatus.COMPANY
    );

    // when
    commentService.updateComment(createCommentResponse.getId(), request);

    // then
    Comment comment = commentRepository.findById(createCommentResponse.getId()).orElseThrow();
    assertThat(comment)
        .extracting("content", "shareStatus")
        .containsExactly("Great meal", ShareStatus.COMPANY);
  }

  @Test
  @DisplayName("Update comment - Fail(Comment not found)")
  void update_comment_fail() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    // Not creating comment

    UpdateCommentRequest request = UpdateCommentRequest.create(
        "Great meal", ShareStatus.COMPANY
    );

    // when // then
    assertThatThrownBy(() -> commentService.updateComment(1L, request))
        .isInstanceOf(CommentNotFoundException.class);
  }

  @Test
  @DisplayName("Delete comment - Success")
  void delete_comment() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    Diner diner = saveDiner("HotTaco");

    CreateCommentRequest createCommentRequest = createCreateCommentRequest("It's delicious");
    CreateCommentResponse createCommentResponse = commentService.createComment(diner.getId(),
        createCommentRequest);

    // when
    commentService.deleteComment(createCommentResponse.getId());

    // then
    Optional<Comment> comment = commentRepository.findById(createCommentResponse.getId());
    assertThat(comment).isEmpty();
  }

  @Test
  @DisplayName("Delete comment - Fail(Comment not found)")
  void delete_comment_no_comment() {
    // given
    Company company = saveCompany();
    saveMember();
    chooseCompany(company);
    saveDiner("HotTaco");

    // Not creating comment

    // when // then
    assertThatThrownBy(() -> commentService.deleteComment(1L))
        .isInstanceOf(CommentNotFoundException.class);
  }

  // --- Create some request ---

  private CreateCommentRequest createCreateCommentRequest(String content) {
    return CreateCommentRequest.create(content, ShareStatus.COMPANY);
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

  private Diner saveDiner(String name) {
    CreateDinerRequest request = CreateDinerRequest.builder()
        .name(name)
        .link("diner.com")
        .latitude(37.29283882)
        .longitude(127.39232323)
        .tags(new LinkedHashSet<>())
        .build();
    CreateDinerResponse response = dinerService.createDiner(request);
    return dinerRepository.findById(response.getId()).orElseThrow();
  }
}