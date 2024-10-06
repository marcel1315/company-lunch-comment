package com.marceldev.ourcompanylunch.service;

import com.marceldev.ourcompanylunch.dto.comment.CommentOutputDto;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentRequest;
import com.marceldev.ourcompanylunch.dto.comment.CreateCommentResponse;
import com.marceldev.ourcompanylunch.dto.comment.GetCommentListRequest;
import com.marceldev.ourcompanylunch.dto.comment.UpdateCommentRequest;
import com.marceldev.ourcompanylunch.entity.Comment;
import com.marceldev.ourcompanylunch.entity.Diner;
import com.marceldev.ourcompanylunch.entity.Member;
import com.marceldev.ourcompanylunch.exception.comment.CommentNotFoundException;
import com.marceldev.ourcompanylunch.exception.diner.DinerNotFoundException;
import com.marceldev.ourcompanylunch.exception.member.MemberNotFoundException;
import com.marceldev.ourcompanylunch.repository.comment.CommentRepository;
import com.marceldev.ourcompanylunch.repository.diner.DinerRepository;
import com.marceldev.ourcompanylunch.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentService {

  private final CommentRepository commentRepository;

  private final DinerRepository dinerRepository;

  private final MemberRepository memberRepository;

  @Transactional
  public CreateCommentResponse createComment(long dinerId, CreateCommentRequest dto) {
    Member member = getMember();

    Diner diner = dinerRepository.findById(dinerId)
        .filter((d) -> d.getCompany().getId().equals(member.getCompany().getId()))
        .orElseThrow(() -> new DinerNotFoundException(dinerId));

    Comment comment = Comment.builder()
        .content(dto.getContent())
        .shareStatus(dto.getShareStatus())
        .diner(diner)
        .member(member)
        .build();

    comment = commentRepository.save(comment);
    return CreateCommentResponse.builder().id(comment.getId()).build();
  }

  public Page<CommentOutputDto> getCommentList(long dinerId,
      GetCommentListRequest dto) {
    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );
    Member member = getMember();
    return commentRepository.getList(dto, member.getId(), dinerId, pageable)
        .map(c -> CommentOutputDto.of(c, c.getMember().getName()));
  }

  @Transactional
  public void updateComment(long commentId, UpdateCommentRequest dto) {
    String email = getMemberEmail();
    Comment comment = commentRepository.findByIdAndMember_Email(commentId, email)
        .orElseThrow(CommentNotFoundException::new);
    comment.setContent(dto.getContent());
    comment.setShareStatus(dto.getShareStatus());
  }

  @Transactional
  public void deleteComment(long commentId) {
    String email = getMemberEmail();
    Comment comment = commentRepository.findByIdAndMember_Email(commentId, email)
        .orElseThrow(CommentNotFoundException::new);
    commentRepository.delete(comment);
  }

  private Member getMember() {
    String email = getMemberEmail();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }

  /**
   * Doesn't query DB. SecurityContextHolder principal has email(username)
   */
  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
