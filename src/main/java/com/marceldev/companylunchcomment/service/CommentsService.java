package com.marceldev.companylunchcomment.service;

import com.marceldev.companylunchcomment.dto.comments.CommentsOutputDto;
import com.marceldev.companylunchcomment.dto.comments.CreateCommentDto;
import com.marceldev.companylunchcomment.dto.comments.GetCommentsListDto;
import com.marceldev.companylunchcomment.dto.comments.UpdateCommentsDto;
import com.marceldev.companylunchcomment.entity.Comments;
import com.marceldev.companylunchcomment.entity.Diner;
import com.marceldev.companylunchcomment.entity.Member;
import com.marceldev.companylunchcomment.exception.CommentsNotFoundException;
import com.marceldev.companylunchcomment.exception.DinerNotFoundException;
import com.marceldev.companylunchcomment.exception.MemberNotExistException;
import com.marceldev.companylunchcomment.repository.comments.CommentsRepository;
import com.marceldev.companylunchcomment.repository.diner.DinerRepository;
import com.marceldev.companylunchcomment.repository.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentsService {

  private final CommentsRepository commentsRepository;

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

    Comments comments = Comments.builder()
        .content(dto.getContent())
        .shareStatus(dto.getShareStatus())
        .diner(diner)
        .member(member)
        .build();

    commentsRepository.save(comments);
  }

  /**
   * 식당의 코멘트 조회
   */
  public Page<CommentsOutputDto> getCommentsList(long dinerId,
      GetCommentsListDto dto, Pageable pageable) {
    Member member = getMember();
    return commentsRepository.getList(dto, member.getId(), dinerId, pageable)
        .map(c -> CommentsOutputDto.of(c, c.getMember().getName()));
  }

  /**
   * 코멘트 수정. 자신의 이메일로 되어 있는 코멘트만 수정 가능함
   */
  @Transactional
  public void updateComments(long commentsId, UpdateCommentsDto dto) {
    String email = getMemberEmail();
    Comments comments = commentsRepository.findByIdAndMember_Email(commentsId, email)
        .orElseThrow(CommentsNotFoundException::new);
    comments.setContent(dto.getContent());
    comments.setShareStatus(dto.getShareStatus());
  }

  /**
   * 코멘트 삭제. 자신의 이메일로 되어 있는 코멘트만 삭제 가능함
   */
  @Transactional
  public void deleteComments(long commentsId) {
    String email = getMemberEmail();
    Comments comments = commentsRepository.findByIdAndMember_Email(commentsId, email)
        .orElseThrow(CommentsNotFoundException::new);
    commentsRepository.delete(comments);
  }

  /**
   * member를 찾아 반환함.
   */
  private Member getMember() {
    String email = getMemberEmail();
    return memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);
  }

  /**
   * member email을 반환함. DB 호출을 하지 않고, SecurityContextHolder에 저장된 것을 사용
   */
  private String getMemberEmail() {
    UserDetails user = (UserDetails) SecurityContextHolder.getContext()
        .getAuthentication()
        .getPrincipal();
    return user.getUsername();
  }
}
