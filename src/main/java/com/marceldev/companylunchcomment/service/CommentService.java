package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.comment.CommentOutputDto;
import com.marceldev.companylunchcomment.dto.comment.CreateCommentDto;
import com.marceldev.companylunchcomment.dto.comment.GetCommentListDto;
import com.marceldev.companylunchcomment.dto.comment.UpdateCommentDto;
import com.marceldev.companylunchcomment.entity.Comment;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.comment.CommentNotFoundException;
import com.marceldev.companylunchcomment.exception.diner.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.member.MemberNotFoundException;
import com.marceldev.companylunchcomment.repository.comment.CommentRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
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

  /**
   * 식당에 코멘트 작성
   */
  @Transactional
  public void createComment(long dinerId, CreateCommentDto dto) {
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

    commentRepository.save(comment);
  }

  /**
   * 식당의 코멘트 조회
   */
  public Page<CommentOutputDto> getCommentList(long dinerId,
      GetCommentListDto dto) {
    Pageable pageable = PageRequest.of(
        dto.getPage(),
        dto.getSize()
    );
    Member member = getMember();
    return commentRepository.getList(dto, member.getId(), dinerId, pageable)
        .map(c -> CommentOutputDto.of(c, c.getMember().getName()));
  }

  /**
   * 코멘트 수정. 자신의 이메일로 되어 있는 코멘트만 수정 가능함
   */
  @Transactional
  public void updateComment(long commentId, UpdateCommentDto dto) {
    String email = getMemberEmail();
    Comment comment = commentRepository.findByIdAndMember_Email(commentId, email)
        .orElseThrow(CommentNotFoundException::new);
    comment.setContent(dto.getContent());
    comment.setShareStatus(dto.getShareStatus());
  }

  /**
   * 코멘트 삭제. 자신의 이메일로 되어 있는 코멘트만 삭제 가능함
   */
  @Transactional
  public void deleteComment(long commentId) {
    String email = getMemberEmail();
    Comment comment = commentRepository.findByIdAndMember_Email(commentId, email)
        .orElseThrow(CommentNotFoundException::new);
    commentRepository.delete(comment);
  }

  /**
   * member를 찾아 반환함.
   */
  private Member getMember() {
    String email = getMemberEmail();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotFoundException::new);
  }

  /**
   * member email을 반환함. DB 호출을 하지 않고, SecurityContextHolder에 저장된 것을 사용
   */
  private String getMemberEmail() {
    return (String) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
  }
}
