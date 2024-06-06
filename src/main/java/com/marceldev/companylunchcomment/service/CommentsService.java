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
import com.marceldev.companylunchcomment.repository.CommentsRepository;
import com.marceldev.companylunchcomment.repository.DinerRepository;
import com.marceldev.companylunchcomment.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
  public void createComment(long dinerId, CreateCommentDto dto, String email) {
    Member member = memberRepository.findByEmail(email)
        .orElseThrow(MemberNotExistException::new);

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
  public Page<CommentsOutputDto> getCommentsList(long dinerId, String email,
      GetCommentsListDto dto, Pageable pageable) {
    // TODO: Check diner exist

    return commentsRepository.getList(dto, pageable);
  }

  /**
   * 코멘트 수정. 자신의 이메일로 되어 있는 코멘트만 수정 가능함
   */
  @Transactional
  public void updateComments(long commentsId, String email, UpdateCommentsDto dto) {
    Comments comments = commentsRepository.findByIdAndMember_Email(commentsId, email)
        .orElseThrow(CommentsNotFoundException::new);
    comments.setContent(dto.getContent());
    comments.setShareStatus(dto.getShareStatus());
  }

  /**
   * 코멘트 삭제. 자신의 이메일로 되어 있는 코멘트만 삭제 가능함
   */
  @Transactional
  public void deleteComments(long commentsId, String email) {
    Comments comments = commentsRepository.findByIdAndMember_Email(commentsId, email)
        .orElseThrow(CommentsNotFoundException::new);
    commentsRepository.delete(comments);
  }
}
